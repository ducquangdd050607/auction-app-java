package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.*;
import com.auctionapp.auctionappjava.common.exception.*;
import com.auctionapp.auctionappjava.common.model.*;
import com.auctionapp.auctionappjava.common.observer.AuctionEventPublisher;
import com.auctionapp.auctionappjava.common.strategy.AuctionExtensionStrategy;
import com.auctionapp.auctionappjava.server.dao.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public class AuctionLifecycleService {
    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final AuctionEventPublisher publisher;
    private final AuctionExtensionStrategy extensionStrategy;
    private final AuctionLockManager lockManager;
    private Function<Auction, AuctionSummaryDto> summaryMapper = a -> null;

    public AuctionLifecycleService(AuctionDao auctionDao, BidDao bidDao, AuctionItemDao itemDao, AuctionEventPublisher publisher, AuctionExtensionStrategy extensionStrategy, AuctionLockManager lockManager) {
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.publisher = publisher;
        this.extensionStrategy = extensionStrategy;
        this.lockManager = lockManager;
    }

    public void setSummaryMapper(Function<Auction, AuctionSummaryDto> summaryMapper) {
        this.summaryMapper = summaryMapper;
    }

    public List<Auction> activateDueAuctions() {
        List<Auction> list = auctionDao.findStartingBefore(LocalDateTime.now());
        for (Auction a : list) lockManager.executeWithLock(a.getId(), () -> activateAuction(a.getId()));
        return list;
    }

    public List<Auction> closeExpiredAuctions() {
        List<Auction> list = auctionDao.findEndingBefore(LocalDateTime.now());
        for (Auction a : list) lockManager.executeWithLock(a.getId(), () -> closeAuction(a.getId()));
        return list;
    }

    public Auction activateAuction(UUID id) {
        Auction a = find(id);
        if (a.getStatus() == AuctionStatus.OPEN && !LocalDateTime.now().isBefore(a.getStartTime())) {
            a.setStatus(AuctionStatus.RUNNING);
            auctionDao.update(a);
            publish(a, "Phiên đấu giá đã bắt đầu");
        }
        return a;
    }

    public Auction closeAuction(UUID id) {
        Auction a = find(id);
        if (a.getStatus().isClosedForBidding()) return a;
        Optional<BidTransaction> h = bidDao.findHighestByAuctionId(id);
        a.setWinnerId(h.map(BidTransaction::getBidderId).orElse(a.getLeadingBidderId()));
        h.ifPresent(b -> {
            a.setCurrentPrice(b.getAmount());
            a.setLeadingBidderId(b.getBidderId());
        });
        a.setStatus(AuctionStatus.FINISHED);
        auctionDao.update(a);
        publish(a, "Phiên đấu giá đã kết thúc");
        return a;
    }

    public Auction markPaid(UUID id) {
        Auction a = find(id);
        if (a.getStatus() != AuctionStatus.FINISHED) throw new ConflictException("Chỉ phiên FINISHED mới được PAID");
        a.setStatus(AuctionStatus.PAID);
        auctionDao.update(a);
        publish(a, "Phiên đấu giá đã thanh toán");
        return a;
    }

    public Auction applyAntiSniping(Auction a, LocalDateTime bidTime) {
        if (extensionStrategy != null && extensionStrategy.shouldExtend(a, bidTime)) {
            a.setEndTime(extensionStrategy.extendTo(a, bidTime));
            auctionDao.update(a);
            publish(a, "Phiên được gia hạn chống sniping");
        }
        return a;
    }

    public Auction refreshState(Auction a) {
        LocalDateTime now = LocalDateTime.now();
        if (a.getStatus() == AuctionStatus.OPEN && !now.isBefore(a.getStartTime()) && now.isBefore(a.getEndTime()))
            return activateAuction(a.getId());
        if ((a.getStatus() == AuctionStatus.OPEN || a.getStatus() == AuctionStatus.RUNNING) && !now.isBefore(a.getEndTime()))
            return closeAuction(a.getId());
        return a;
    }

    private Auction find(UUID id) {
        return auctionDao.findById(id).orElseThrow(() -> new ValidationException("Không tìm thấy auction"));
    }

    private void publish(Auction a, String msg) {
        publisher.publish(new AuctionEventDto(EventType.AUCTION_STATUS_CHANGED, a.getId(), summaryMapper.apply(a), null, msg, LocalDateTime.now()));
    }
}
