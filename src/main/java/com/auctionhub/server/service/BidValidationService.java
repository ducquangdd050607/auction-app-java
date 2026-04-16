package com.auctionhub.server.service;

import com.auctionhub.common.exception.ValidationException;
import com.auctionhub.common.model.Auction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class BidValidationService {
    public void validateBid(Auction auction, UUID bidderId, BigDecimal amount, LocalDateTime now) {
        if (auction == null) {
            throw new ValidationException("Không tìm thấy phiên đấu giá.");
        }
        if (bidderId == null) {
            throw new ValidationException("Người dùng chưa đăng nhập.");
        }
        if (!auction.isAcceptingBids(now)) {
            throw new ValidationException("Phiên đấu giá hiện không nhận giá mới.");
        }
        if (auction.getSellerId().equals(bidderId)) {
            throw new ValidationException("Người bán không thể tự đặt giá cho sản phẩm của mình.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Giá đặt phải lớn hơn 0.");
        }
        BigDecimal minimumAccepted = auction.getCurrentPrice().add(auction.getMinimumIncrement());
        if (amount.compareTo(minimumAccepted) < 0) {
            throw new ValidationException("Giá đặt phải lớn hơn hoặc bằng " + minimumAccepted + ".");
        }
    }

    public void validateAutoBid(Auction auction, UUID bidderId, BigDecimal maxBid, BigDecimal increment, LocalDateTime now) {
        validateBid(auction, bidderId, maxBid.max(auction.getCurrentPrice().add(auction.getMinimumIncrement())), now);
        if (increment == null || increment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Bước giá auto-bid phải lớn hơn 0.");
        }
        if (maxBid.compareTo(auction.getCurrentPrice().add(auction.getMinimumIncrement())) < 0) {
            throw new ValidationException("maxBid phải ít nhất bằng giá hiện tại cộng bước giá tối thiểu.");
        }
    }
}
