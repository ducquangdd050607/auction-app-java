package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.exception.*;
import com.auctionapp.auctionappjava.common.model.*;
import com.auctionapp.auctionappjava.common.util.ValidationUtils;
import com.auctionapp.auctionappjava.server.dao.UserDao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BidValidationService {
    private final UserDao userDao;
    private final WalletService walletService;

    public BidValidationService(UserDao userDao, WalletService walletService) {
        this.userDao = userDao;
        this.walletService = walletService;
    }

    public User validateBid(Auction auction, UUID bidderId, BigDecimal amount) {
        if (auction == null || bidderId == null) throw new ValidationException("Thiếu auction/bidder");
        ValidationUtils.requirePositive(amount, "Giá đặt");
        User bidder = validateBidderBasics(auction, bidderId);
        if (auction.getStatus() != AuctionStatus.RUNNING || !auction.isAcceptingBids(LocalDateTime.now()))
            throw new ConflictException("Phiên chưa mở hoặc đã đóng");
        BigDecimal required = auction.getCurrentPrice().add(auction.getMinimumIncrement());
        if (amount.compareTo(required) < 0) throw new ValidationException("Giá đặt phải tối thiểu là " + required);
        BigDecimal needed = bidderId.equals(auction.getLeadingBidderId()) ? amount.subtract(auction.getCurrentPrice()) : amount;
        if (needed.compareTo(BigDecimal.ZERO) > 0) walletService.ensureSufficientBalance(bidderId, needed);
        return bidder;
    }

    public void validateAutoBid(Auction auction, AutoBidConfig config) {
        if (config == null) throw new ValidationException("Thiếu cấu hình auto bid");
        validateBidderBasics(auction, config.getBidderId());
        ValidationUtils.requirePositive(config.getMaxBid(), "Max bid");
        ValidationUtils.requirePositive(config.getIncrementAmount(), "Bước auto bid");
        if (config.getIncrementAmount().compareTo(auction.getMinimumIncrement()) < 0)
            throw new ValidationException("Bước auto bid phải >= bước tối thiểu");
        if (config.getMaxBid().compareTo(auction.getCurrentPrice().add(auction.getMinimumIncrement())) < 0)
            throw new ValidationException("Max bid chưa đủ cao");
        walletService.ensureSufficientBalance(config.getBidderId(), config.getMaxBid());
    }

    public User validateBidderBasics(Auction auction, UUID bidderId) {
        User bidder = userDao.findById(bidderId).orElseThrow(() -> new AuthException("Không tìm thấy bidder"));
        if (!bidder.isActive()) throw new AuthException("Tài khoản bị khóa");
        if (bidder.getRole() != Role.BIDDER) throw new AuthorizationException("Chỉ BIDDER được đặt giá");
        if (bidderId.equals(auction.getSellerId())) throw new AuthorizationException("Seller không được tự bid");
        return bidder;
    }
}
