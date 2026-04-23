package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.config.AppConstants;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.*;
import com.auctionapp.auctionappjava.common.exception.*;
import com.auctionapp.auctionappjava.common.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.common.model.*;
import com.auctionapp.auctionappjava.common.observer.AuctionEventPublisher;
import com.auctionapp.auctionappjava.common.util.ValidationUtils;
import com.auctionapp.auctionappjava.server.dao.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class AuctionService {
    private final AuctionDao auctionDao;
    private final AuctionItemDao itemDao;
    private final BidDao bidDao;
    private final AutoBidDao autoBidDao;
    private final UserDao userDao;
    private final WalletService walletService;
    private final BidValidationService bidValidationService;
    private final AuctionLockManager lockManager;
    private final AutoBidEngine autoBidEngine;
    private final AuctionLifecycleService lifecycleService;
    private final AuctionEventPublisher eventPublisher;

    public AuctionService(AuctionDao auctionDao, AuctionItemDao itemDao, BidDao bidDao, AutoBidDao autoBidDao, UserDao userDao, WalletService walletService, BidValidationService bidValidationService, AuctionLockManager lockManager, AutoBidEngine autoBidEngine, AuctionLifecycleService lifecycleService, AuctionEventPublisher eventPublisher) {
        this.auctionDao = auctionDao;
        this.itemDao = itemDao;
        this.bidDao = bidDao;
        this.autoBidDao = autoBidDao;
        this.userDao = userDao;
        this.walletService = walletService;
        this.bidValidationService = bidValidationService;
        this.lockManager = lockManager;
        this.autoBidEngine = autoBidEngine;
        this.lifecycleService = lifecycleService;
        this.eventPublisher = eventPublisher;
        this.lifecycleService.setSummaryMapper(this::toSummary);
    }

    public AuctionSummaryDto createAuction(CreateAuctionRequest r) {
        if (r == null) throw new ValidationException("Thiếu thông tin tạo auction");
        User seller = userDao.findById(r.sellerId()).orElseThrow(() -> new AuthException("Không tìm thấy seller"));
        if (!seller.isActive() || seller.getRole() != Role.SELLER)
            throw new AuthorizationException("Chỉ SELLER được tạo auction");
        String title = ValidationUtils.requireText(r.title(), "Tên sản phẩm");
        String desc = ValidationUtils.requireText(r.description(), "Mô tả");
        ValidationUtils.requirePositive(r.startingPrice(), "Giá khởi điểm");
        BigDecimal inc = r.minimumIncrement() == null ? AppConstants.DEFAULT_MIN_INCREMENT : r.minimumIncrement();
        ValidationUtils.requirePositive(inc, "Bước giá");
        ValidationUtils.requireTimeRange(r.startTime(), r.endTime());
        if (!r.endTime().isAfter(LocalDateTime.now()))
            throw new ValidationException("Thời gian kết thúc phải ở tương lai");
        Item item = AuctionItemFactory.create(r.itemType(), UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), r.sellerId(), title, desc, r.startingPrice(), r.attributeOne(), r.attributeTwo());
        itemDao.save(item);
        Auction a = new Auction();
        a.setItemId(item.getId());
        a.setSellerId(r.sellerId());
        a.setCurrentPrice(r.startingPrice());
        a.setMinimumIncrement(inc);
        a.setStartTime(r.startTime());
        a.setEndTime(r.endTime());
        a.setStatus(LocalDateTime.now().isBefore(r.startTime()) ? AuctionStatus.OPEN : AuctionStatus.RUNNING);
        auctionDao.save(a);
        AuctionSummaryDto s = toSummary(a);
        publish(EventType.AUCTION_UPDATED, a.getId(), s, null, "Tạo phiên đấu giá mới");
        return s;
    }

    public AuctionSummaryDto updateAuction(UpdateAuctionRequest r) {
        if (r == null || r.auctionId() == null) throw new ValidationException("Thiếu thông tin cập nhật");
        return lockManager.executeWithLock(r.auctionId(), () -> {
            Auction a = find(r.auctionId());
            ensureOwnerOrAdmin(r.actorId(), a);
            if (a.getStatus().isClosedForBidding()) throw new ConflictException("Phiên đã đóng");
            Item item = itemDao.findById(a.getItemId()).orElseThrow(() -> new ValidationException("Không tìm thấy item"));
            item.setTitle(ValidationUtils.requireText(r.title(), "Tên sản phẩm"));
            item.setDescription(ValidationUtils.requireText(r.description(), "Mô tả"));
            ValidationUtils.requirePositive(r.startingPrice(), "Giá khởi điểm");
            item.setStartingPrice(r.startingPrice());
            if (r.itemType() != null) item.setItemType(r.itemType());
            item.setAttributeOne(r.attributeOne());
            item.setAttributeTwo(r.attributeTwo());
            itemDao.update(item);
            ValidationUtils.requireTimeRange(r.startTime(), r.endTime());
            a.setStartTime(r.startTime());
            a.setEndTime(r.endTime());
            if (r.minimumIncrement() != null) a.setMinimumIncrement(r.minimumIncrement());
            if (bidDao.countByAuctionId(a.getId()) == 0) a.setCurrentPrice(r.startingPrice());
            auctionDao.update(a);
            AuctionSummaryDto s = toSummary(a);
            publish(EventType.AUCTION_UPDATED, a.getId(), s, null, "Cập nhật auction");
            return s;
        });
    }

    public List<AuctionSummaryDto> listAuctions() {
        lifecycleService.activateDueAuctions();
        lifecycleService.closeExpiredAuctions();
        List<AuctionSummaryDto> out = new ArrayList<>();
        for (Auction a : auctionDao.findAll()) out.add(toSummary(lifecycleService.refreshState(a)));
        return out;
    }

    public List<AuctionSummaryDto> listMyAuctions(UUID userId) {
        if (userId == null) throw new ValidationException("Thiếu userId");
        User u = userDao.findById(userId).orElseThrow(() -> new AuthException("Không tìm thấy user"));
        List<AuctionSummaryDto> out = new ArrayList<>();
        if (u.getRole() == Role.SELLER) {
            for (Auction a : auctionDao.findBySellerId(userId)) out.add(toSummary(lifecycleService.refreshState(a)));
        } else {
            Set<UUID> ids = new LinkedHashSet<>();
            for (BidTransaction b : bidDao.findByBidderId(userId)) ids.add(b.getAuctionId());
            for (UUID id : ids)
                auctionDao.findById(id).ifPresent(a -> out.add(toSummary(lifecycleService.refreshState(a))));
        }
        return out;
    }

    public AuctionDetailDto getAuctionDetail(UUID auctionId) {
        Auction a = lifecycleService.refreshState(find(auctionId));
        Item item = itemDao.findById(a.getItemId()).orElseThrow(() -> new ValidationException("Không tìm thấy item"));
        List<BidDto> bids = new ArrayList<>();
        for (BidTransaction b : bidDao.findByAuctionId(auctionId)) bids.add(toBidDto(b));
        return new AuctionDetailDto(toSummary(a), item.getAttributeOne(), item.getAttributeTwo(), bids);
    }

    public AuctionDetailDto placeBid(PlaceBidRequest r) {
        if (r == null || r.auctionId() == null) throw new ValidationException("Thiếu thông tin bid");
        return lockManager.executeWithLock(r.auctionId(), () -> {
            Auction a = lifecycleService.refreshState(find(r.auctionId()));
            bidValidationService.validateBid(a, r.bidderId(), r.amount());
            BidTransaction b = applyBid(a, r.bidderId(), r.amount(), false, "MANUAL_BID");
            a = lifecycleService.applyAntiSniping(find(a.getId()), b.getCreatedAt());
            publish(EventType.BID_PLACED, a.getId(), toSummary(a), toBidDto(b), "Có bid mới");
            for (BidTransaction auto : autoBidEngine.reactToBid(a.getId())) {
                Auction now = find(a.getId());
                publish(EventType.BID_PLACED, now.getId(), toSummary(now), toBidDto(auto), "Auto bid được kích hoạt");
            }
            return getAuctionDetail(a.getId());
        });
    }

    public AutoBidConfig configureAutoBid(AutoBidRequest r) {
        if (r == null || r.auctionId() == null || r.bidderId() == null) throw new ValidationException("Thiếu auto bid");
        return lockManager.executeWithLock(r.auctionId(), () -> {
            Auction a = lifecycleService.refreshState(find(r.auctionId()));
            AutoBidConfig c = new AutoBidConfig();
            c.setAuctionId(r.auctionId());
            c.setBidderId(r.bidderId());
            c.setMaxBid(r.maxBid());
            c.setIncrementAmount(r.incrementAmount());
            c.setEnabled(r.enabled());
            AutoBidConfig saved = autoBidEngine.configure(a, c);
            publish(EventType.AUCTION_UPDATED, a.getId(), toSummary(a), null, "Cập nhật auto bid");
            return saved;
        });
    }

    public AuctionSummaryDto cancelAuction(UUID auctionId, UUID actorId) {
        return lockManager.executeWithLock(auctionId, () -> {
            Auction a = find(auctionId);
            ensureOwnerOrAdmin(actorId, a);
            if (a.getStatus().isClosedForBidding()) throw new ConflictException("Phiên đã đóng");
            if (a.getLeadingBidderId() != null && a.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0)
                walletService.credit(a.getLeadingBidderId(), a.getCurrentPrice());
            a.setStatus(AuctionStatus.CANCELED);
            auctionDao.update(a);
            AuctionSummaryDto s = toSummary(a);
            publish(EventType.AUCTION_STATUS_CHANGED, auctionId, s, null, "Phiên đã bị hủy");
            return s;
        });
    }

    public AuctionSummaryDto markPaid(UUID auctionId) {
        return lockManager.executeWithLock(auctionId, () -> toSummary(lifecycleService.markPaid(auctionId)));
    }

    public void deleteAuction(UUID auctionId, UUID actorId) {
        lockManager.executeWithLock(auctionId, () -> {
            Auction a = find(auctionId);
            ensureOwnerOrAdmin(actorId, a);
            if (a.getStatus() == AuctionStatus.RUNNING) throw new ConflictException("Không xóa phiên đang chạy");
            auctionDao.delete(auctionId);
        });
    }

    public AuctionSummaryDto toSummary(Auction a) {
        Item item = itemDao.findById(a.getItemId()).orElse(null);
        String lead = a.getLeadingBidderId() == null ? null : userDao.findById(a.getLeadingBidderId()).map(User::getUsername).orElse(null);
        return new AuctionSummaryDto(a.getId(), a.getItemId(), a.getSellerId(), item == null ? "(missing item)" : item.getTitle(), item == null ? "" : item.getDescription(), item == null ? null : item.getItemType(), item == null ? BigDecimal.ZERO : item.getStartingPrice(), a.getCurrentPrice(), a.getMinimumIncrement(), a.getStatus(), a.getStartTime(), a.getEndTime(), a.getLeadingBidderId(), lead, a.getWinnerId(), bidDao.countByAuctionId(a.getId()));
    }

    public BidDto toBidDto(BidTransaction b) {
        String name = userDao.findById(b.getBidderId()).map(User::getUsername).orElse("unknown");
        return new BidDto(b.getId(), b.getAuctionId(), b.getBidderId(), name, b.getAmount(), b.getCreatedAt(), b.isAutoGenerated(), b.getNote());
    }

    private BidTransaction applyBid(Auction a, UUID bidderId, BigDecimal amount, boolean auto, String note) {
        UUID prev = a.getLeadingBidderId();
        BigDecimal prevPrice = a.getCurrentPrice();
        if (prev != null && prev.equals(bidderId)) {
            BigDecimal delta = amount.subtract(prevPrice);
            if (delta.compareTo(BigDecimal.ZERO) > 0) walletService.debit(bidderId, delta);
        } else {
            walletService.debit(bidderId, amount);
            if (prev != null && prevPrice.compareTo(BigDecimal.ZERO) > 0) walletService.credit(prev, prevPrice);
        }
        BidTransaction b = new BidTransaction();
        b.setAuctionId(a.getId());
        b.setBidderId(bidderId);
        b.setAmount(amount);
        b.setAutoGenerated(auto);
        b.setNote(note);
        bidDao.save(b);
        a.setCurrentPrice(amount);
        a.setLeadingBidderId(bidderId);
        auctionDao.update(a);
        return b;
    }

    private Auction find(UUID id) {
        return auctionDao.findById(id).orElseThrow(() -> new ValidationException("Không tìm thấy auction"));
    }

    private void ensureOwnerOrAdmin(UUID actorId, Auction a) {
        if (actorId == null) throw new AuthorizationException("Thiếu actorId");
        User actor = userDao.findById(actorId).orElseThrow(() -> new AuthException("Không tìm thấy user"));
        if (actor.getRole() != Role.ADMIN && !actorId.equals(a.getSellerId()))
            throw new AuthorizationException("Không có quyền");
    }

    private void publish(EventType t, UUID id, AuctionSummaryDto a, BidDto b, String msg) {
        eventPublisher.publish(new AuctionEventDto(t, id, a, b, msg, LocalDateTime.now()));
    }
}
