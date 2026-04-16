package com.auctionhub.server.service;

import com.auctionhub.common.dto.AdminOverviewDto;
import com.auctionhub.common.dto.AuctionDetailDto;
import com.auctionhub.common.dto.AuctionSummaryDto;
import com.auctionhub.common.dto.AuthUserDto;
import com.auctionhub.common.dto.AutoBidRequest;
import com.auctionhub.common.dto.BidDto;
import com.auctionhub.common.dto.CreateAuctionRequest;
import com.auctionhub.common.dto.UpdateAuctionRequest;
import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.enums.EventType;
import com.auctionhub.common.enums.Role;
import com.auctionhub.common.exception.AuthorizationException;
import com.auctionhub.common.exception.ValidationException;
import com.auctionhub.common.factory.AuctionItemFactory;
import com.auctionhub.common.model.Auction;
import com.auctionhub.common.model.AuctionItem;
import com.auctionhub.common.model.AutoBidConfig;
import com.auctionhub.common.model.BidTransaction;
import com.auctionhub.common.model.User;
import com.auctionhub.common.observer.AuctionEventPublisher;
import com.auctionhub.common.util.JacksonSupport;
import com.auctionhub.common.util.TimeUtils;
import com.auctionhub.common.util.ValidationUtils;
import com.auctionhub.server.dao.AuctionDao;
import com.auctionhub.server.dao.AuctionItemDao;
import com.auctionhub.server.dao.AutoBidDao;
import com.auctionhub.server.dao.BidDao;
import com.auctionhub.server.dao.UserDao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionService {
    private final AuctionItemDao auctionItemDao;
    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final AutoBidDao autoBidDao;
    private final UserDao userDao;
    private final BidValidationService bidValidationService;
    private final AuctionLifecycleService lifecycleService;
    private final AutoBidEngine autoBidEngine;
    private final AuctionLockManager auctionLockManager;
    private final AuctionEventPublisher publisher;
    private DashboardService dashboardService;

    public AuctionService(AuctionItemDao auctionItemDao,
                          AuctionDao auctionDao,
                          BidDao bidDao,
                          AutoBidDao autoBidDao,
                          UserDao userDao,
                          BidValidationService bidValidationService,
                          AuctionLifecycleService lifecycleService,
                          AutoBidEngine autoBidEngine,
                          AuctionLockManager auctionLockManager,
                          AuctionEventPublisher publisher) {
        this.auctionItemDao = auctionItemDao;
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.autoBidDao = autoBidDao;
        this.userDao = userDao;
        this.bidValidationService = bidValidationService;
        this.lifecycleService = lifecycleService;
        this.autoBidEngine = autoBidEngine;
        this.auctionLockManager = auctionLockManager;
        this.publisher = publisher;
    }

    public void attachDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public List<AuctionSummaryDto> listAuctions(UUID viewerId) {
        LocalDateTime now = LocalDateTime.now();
        return auctionDao.findAll().stream()
                .peek(auction -> refreshAuctionStatus(auction, false))
                .sorted(Comparator.comparing(Auction::getEndTime))
                .map(auction -> toSummary(auction, viewerId, now))
                .toList();
    }

    public List<AuctionSummaryDto> listAuctionsBySeller(UUID sellerId) {
        LocalDateTime now = LocalDateTime.now();
        return auctionDao.findBySellerId(sellerId).stream()
                .peek(auction -> refreshAuctionStatus(auction, false))
                .map(auction -> toSummary(auction, sellerId, now))
                .toList();
    }

    public AuctionDetailDto getAuctionDetail(UUID auctionId, UUID viewerId) {
        Auction auction = requireAuction(auctionId);
        refreshAuctionStatus(auction, false);
        return toDetail(auction, viewerId);
    }

    public AuctionDetailDto createAuction(AuthUserDto actor, CreateAuctionRequest request) {
        requireSellerOrAdmin(actor);
        validateAuctionDraft(request.title(), request.description(), request.startingPrice(), request.minimumIncrement(), request.startTime(), request.endTime());

        AuctionItem item = AuctionItemFactory.create(actor.id(), request);
        auctionItemDao.save(item);

        LocalDateTime now = LocalDateTime.now();
        Auction auction = new Auction(UUID.randomUUID(), now, now, item.getId(), actor.id(), request.startingPrice(), null,
                request.startTime(), request.endTime(), AuctionStatus.OPEN, request.minimumIncrement(), null);
        lifecycleService.refreshStatus(auction, LocalDateTime.now());
        auctionDao.save(auction);

        AuctionDetailDto detail = toDetail(auction, actor.id());
        publishAuctionEvent(auction.getId(), EventType.AUCTION_UPDATED, "Seller vừa tạo phiên đấu giá mới.");
        return detail;
    }

    public AuctionDetailDto updateAuction(AuthUserDto actor, UpdateAuctionRequest request) {
        requireSellerOrAdmin(actor);
        validateAuctionDraft(request.title(), request.description(), request.startingPrice(), request.minimumIncrement(), request.startTime(), request.endTime());
        ReentrantLock lock = auctionLockManager.acquire(request.auctionId());
        try {
            Auction auction = requireAuction(request.auctionId());
            if (!Objects.equals(actor.id(), auction.getSellerId()) && actor.role() != Role.ADMIN) {
                throw new AuthorizationException("Bạn không có quyền sửa phiên đấu giá này.");
            }
            if (auction.getStatus() == AuctionStatus.FINISHED || auction.getStatus() == AuctionStatus.PAID) {
                throw new ValidationException("Phiên đã kết thúc thì không thể chỉnh sửa.");
            }

            AuctionItem item = auctionItemDao.findById(auction.getItemId())
                    .orElseThrow(() -> new ValidationException("Không tìm thấy item."));
            item.setTitle(request.title());
            item.setDescription(request.description());
            item.setStartingPrice(request.startingPrice());
            item.setAttributeOne(request.attributeOne());
            item.setAttributeTwo(request.attributeTwo());
            item.touch();
            auctionItemDao.update(item);

            auction.setStartTime(request.startTime());
            auction.setEndTime(request.endTime());
            auction.setMinimumIncrement(request.minimumIncrement());
            auction.touch();
            refreshAuctionStatus(auction, false);
            auctionDao.update(auction);
            publishAuctionEvent(auction.getId(), EventType.AUCTION_UPDATED, "Phiên đấu giá vừa được cập nhật.");
            return toDetail(auction, actor.id());
        } finally {
            lock.unlock();
        }
    }

    public void deleteAuction(AuthUserDto actor, UUID auctionId) {
        requireSellerOrAdmin(actor);
        ReentrantLock lock = auctionLockManager.acquire(auctionId);
        try {
            Auction auction = requireAuction(auctionId);
            if (!Objects.equals(actor.id(), auction.getSellerId()) && actor.role() != Role.ADMIN) {
                throw new AuthorizationException("Bạn không có quyền xoá phiên đấu giá này.");
            }
            if (auction.getStatus() == AuctionStatus.RUNNING) {
                throw new ValidationException("Không thể xoá phiên đang RUNNING. Hãy chuyển sang CANCELED thay vì xoá cứng.");
            }
            auctionDao.deleteById(auctionId);
            auctionItemDao.deleteById(auction.getItemId());
            autoBidDao.disableByAuctionId(auctionId);
            publisher.publish(auctionId, com.auctionhub.common.dto.ApiEnvelope.event(EventType.SYSTEM_MESSAGE, "Phiên đấu giá đã bị xoá.", JacksonSupport.toNode(new com.auctionhub.common.dto.AuctionIdRequest(auctionId))));
        } finally {
            lock.unlock();
        }
    }

    public AuctionDetailDto cancelAuction(AuthUserDto actor, UUID auctionId) {
        ReentrantLock lock = auctionLockManager.acquire(auctionId);
        try {
            Auction auction = requireAuction(auctionId);
            if (actor.role() != Role.ADMIN && !Objects.equals(actor.id(), auction.getSellerId())) {
                throw new AuthorizationException("Bạn không có quyền hủy phiên này.");
            }
            auction.setStatus(AuctionStatus.CANCELED);
            auction.touch();
            auctionDao.update(auction);
            autoBidDao.disableByAuctionId(auctionId);
            publishAuctionEvent(auctionId, EventType.AUCTION_STATUS_CHANGED, "Phiên đấu giá đã bị hủy.");
            return toDetail(auction, actor.id());
        } finally {
            lock.unlock();
        }
    }

    public AuctionDetailDto markAuctionPaid(AuthUserDto actor, UUID auctionId) {
        if (actor.role() != Role.ADMIN) {
            throw new AuthorizationException("Chỉ admin được quyền đánh dấu PAID.");
        }
        ReentrantLock lock = auctionLockManager.acquire(auctionId);
        try {
            Auction auction = requireAuction(auctionId);
            if (auction.getStatus() != AuctionStatus.FINISHED) {
                throw new ValidationException("Chỉ phiên FINISHED mới được chuyển sang PAID.");
            }
            auction.setStatus(AuctionStatus.PAID);
            auction.touch();
            auctionDao.update(auction);
            publishAuctionEvent(auctionId, EventType.AUCTION_STATUS_CHANGED, "Phiên đấu giá đã được đánh dấu PAID.");
            return toDetail(auction, actor.id());
        } finally {
            lock.unlock();
        }
    }

    public AuctionDetailDto placeBid(AuthUserDto actor, com.auctionhub.common.dto.PlaceBidRequest request) {
        if (actor.role() != Role.BIDDER && actor.role() != Role.ADMIN) {
            throw new AuthorizationException("Chỉ bidder hoặc admin mới được đặt giá.");
        }
        ReentrantLock lock = auctionLockManager.acquire(request.auctionId());
        try {
            Auction auction = requireAuction(request.auctionId());
            refreshAuctionStatus(auction, false);
            bidValidationService.validateBid(auction, actor.id(), request.amount(), LocalDateTime.now());

            List<AutoBidConfig> configs = new ArrayList<>(autoBidDao.findByAuctionId(auction.getId()));
            AutoBidEngine.BidComputationResult result = autoBidEngine.applyManualBid(auction, configs, actor.id(), request.amount(), LocalDateTime.now());
            persistBidResult(result);
            publishAuctionEvent(auction.getId(), EventType.BID_PLACED, "Có bid mới trong phiên đấu giá.");
            return toDetail(result.auction(), actor.id());
        } finally {
            lock.unlock();
        }
    }

    public AuctionDetailDto configureAutoBid(AuthUserDto actor, AutoBidRequest request) {
        if (actor.role() != Role.BIDDER && actor.role() != Role.ADMIN) {
            throw new AuthorizationException("Chỉ bidder hoặc admin mới được cấu hình auto-bid.");
        }
        ReentrantLock lock = auctionLockManager.acquire(request.auctionId());
        try {
            Auction auction = requireAuction(request.auctionId());
            refreshAuctionStatus(auction, false);
            bidValidationService.validateAutoBid(auction, actor.id(), request.maxBid(), request.increment(), LocalDateTime.now());
            LocalDateTime now = LocalDateTime.now();
            AutoBidConfig config = autoBidDao.findByAuctionAndBidder(auction.getId(), actor.id())
                    .orElse(new AutoBidConfig(UUID.randomUUID(), now, now, auction.getId(), actor.id(), request.maxBid(), request.increment(), true));
            config.setMaxBid(request.maxBid());
            config.setIncrementAmount(request.increment());
            config.setEnabled(true);
            config.touch();
            autoBidDao.upsert(config);

            List<AutoBidConfig> configs = autoBidDao.findByAuctionId(auction.getId());
            AutoBidEngine.BidComputationResult result = autoBidEngine.applyAutoBidRegistration(auction, configs, LocalDateTime.now());
            persistBidResult(result);
            publishAuctionEvent(auction.getId(), EventType.BID_PLACED, "Cấu hình auto-bid vừa được cập nhật.");
            return toDetail(result.auction(), actor.id());
        } finally {
            lock.unlock();
        }
    }

    public AdminOverviewDto adminOverview() {
        if (dashboardService == null) {
            throw new IllegalStateException("DashboardService chưa được gắn vào AuctionService.");
        }
        return dashboardService.buildAdminOverview();
    }

    public List<AuctionDetailDto> refreshAuctionStatuses() {
        List<AuctionDetailDto> changed = new ArrayList<>();
        for (Auction auction : auctionDao.findAll()) {
            if (refreshAuctionStatus(auction, true)) {
                changed.add(toDetail(auction, null));
            }
        }
        return changed;
    }

    private boolean refreshAuctionStatus(Auction auction, boolean publish) {
        boolean changed = lifecycleService.refreshStatus(auction, LocalDateTime.now());
        if (changed) {
            auction.touch();
            auctionDao.update(auction);
            if (auction.getStatus().isClosedForBidding()) {
                autoBidDao.disableByAuctionId(auction.getId());
            }
            if (publish) {
                publishAuctionEvent(auction.getId(), EventType.AUCTION_STATUS_CHANGED, "Trạng thái phiên đấu giá vừa thay đổi.");
            }
        }
        return changed;
    }

    private void persistBidResult(AutoBidEngine.BidComputationResult result) {
        result.auction().touch();
        auctionDao.update(result.auction());
        if (!result.generatedBids().isEmpty()) {
            bidDao.saveAll(result.generatedBids());
        }
    }

    private AuctionSummaryDto toSummary(Auction auction, UUID viewerId, LocalDateTime now) {
        AuctionItem item = auctionItemDao.findById(auction.getItemId()).orElseThrow(() -> new ValidationException("Thiếu item cho auction."));
        String sellerName = userDisplayName(auction.getSellerId());
        String leaderName = auction.getLeadingBidderId() == null ? "Chưa có" : userDisplayName(auction.getLeadingBidderId());
        boolean autoBidEnabled = viewerId != null && autoBidDao.findByAuctionAndBidder(auction.getId(), viewerId).isPresent();
        return new AuctionSummaryDto(
                auction.getId(),
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getItemType(),
                item.getStartingPrice(),
                auction.getCurrentPrice(),
                auction.getMinimumIncrement(),
                sellerName,
                leaderName,
                auction.getStartTime(),
                auction.getEndTime(),
                auction.getStatus(),
                TimeUtils.secondsRemaining(auction.getEndTime(), now),
                autoBidEnabled
        );
    }

    private AuctionDetailDto toDetail(Auction auction, UUID viewerId) {
        AuctionItem item = auctionItemDao.findById(auction.getItemId()).orElseThrow(() -> new ValidationException("Thiếu item cho auction."));
        List<BidDto> bidHistory = bidDao.findByAuctionId(auction.getId()).stream().map(this::toBidDto).toList();
        String winnerName = auction.getWinnerId() == null ? "-" : userDisplayName(auction.getWinnerId());
        boolean canBid = viewerId != null && auction.isAcceptingBids(LocalDateTime.now()) && !Objects.equals(viewerId, auction.getSellerId());
        return new AuctionDetailDto(
                toSummary(auction, viewerId, LocalDateTime.now()),
                item.getAttributeOne(),
                item.getAttributeTwo(),
                bidHistory,
                winnerName,
                canBid,
                lifecycleService.explain(auction)
        );
    }

    private BidDto toBidDto(BidTransaction bidTransaction) {
        return new BidDto(
                bidTransaction.getId(),
                bidTransaction.getAuctionId(),
                bidTransaction.getBidderId(),
                userDisplayName(bidTransaction.getBidderId()),
                bidTransaction.getAmount(),
                bidTransaction.isAutoGenerated(),
                bidTransaction.getCreatedAt(),
                bidTransaction.getNote()
        );
    }

    private void publishAuctionEvent(UUID auctionId, EventType eventType, String message) {
        AuctionDetailDto detail = toDetail(requireAuction(auctionId), null);
        publisher.publish(auctionId, com.auctionhub.common.dto.ApiEnvelope.event(eventType, message, JacksonSupport.toNode(detail)));
    }

    private Auction requireAuction(UUID auctionId) {
        return auctionDao.findById(auctionId).orElseThrow(() -> new ValidationException("Không tìm thấy phiên đấu giá."));
    }

    private String userDisplayName(UUID userId) {
        return userDao.findById(userId)
                .map(User::getFullName)
                .orElse("Unknown");
    }

    private void requireSellerOrAdmin(AuthUserDto actor) {
        if (actor.role() != Role.SELLER && actor.role() != Role.ADMIN) {
            throw new AuthorizationException("Chỉ seller hoặc admin được phép thực hiện thao tác này.");
        }
    }

    private void validateAuctionDraft(String title,
                                      String description,
                                      BigDecimal startingPrice,
                                      BigDecimal minimumIncrement,
                                      LocalDateTime startTime,
                                      LocalDateTime endTime) {
        ValidationUtils.requireNotBlank(title, "Tên sản phẩm");
        ValidationUtils.requireNotBlank(description, "Mô tả");
        ValidationUtils.requirePositive(startingPrice, "Giá khởi điểm");
        ValidationUtils.requirePositive(minimumIncrement, "Bước giá tối thiểu");
        if (startTime == null || endTime == null) {
            throw new ValidationException("Phải nhập thời gian bắt đầu và kết thúc.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new ValidationException("Thời gian kết thúc phải sau thời gian bắt đầu.");
        }
    }
}
