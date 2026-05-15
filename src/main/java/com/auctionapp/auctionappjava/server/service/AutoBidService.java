package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.ConfigureAutoBidRequest;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.dao.AutoBidDao;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAuctionDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAutoBidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcUserDao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class AutoBidService {
    private final AuctionDao auctionDao;
    private final AutoBidDao autoBidDao;
    private final UserDao userDao;

    public AutoBidService() {
        this(new JdbcAuctionDao(), new JdbcAutoBidDao(), new JdbcUserDao());
    }

    public AutoBidService(AuctionDao auctionDao, AutoBidDao autoBidDao, UserDao userDao) {
        this.auctionDao = auctionDao;
        this.autoBidDao = autoBidDao;
        this.userDao = userDao;
    }

    public Response handleConfigureAutoBid(ConfigureAutoBidRequest request) {
        try {
            if (request == null) {
                return new Response(false, "Thiếu dữ liệu cấu hình auto-bid.", null);
            }
            if (request.auctionId() == null || request.bidderId() == null) {
                return new Response(false, "auctionId và bidderId không được rỗng.", null);
            }
            if (!request.enabled()) {
                autoBidDao.disableByAuctionIdAndBidderId(request.auctionId(), request.bidderId());
                return new Response(true, "Đã tắt auto-bid.", null);
            }
            if (request.maxBid() == null || request.maxBid().compareTo(BigDecimal.ZERO) <= 0) {
                return new Response(false, "Giá auto-bid tối đa phải lớn hơn 0.", null);
            }
            if (request.incrementAmount() == null || request.incrementAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return new Response(false, "Bước auto-bid phải lớn hơn 0.", null);
            }

            Optional<Auction> auctionOpt = auctionDao.findById(request.auctionId());
            if (auctionOpt.isEmpty()) {
                return new Response(false, "Phiên đấu giá không tồn tại.", null);
            }
            Auction auction = auctionOpt.get();
            if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
                return new Response(false, "Chỉ có thể bật auto-bid cho phiên OPEN/RUNNING.", null);
            }
            if (request.maxBid().compareTo(auction.getCurrentPrice()) <= 0) {
                return new Response(false, "Giá tối đa auto-bid phải lớn hơn giá hiện tại.", null);
            }
            if (request.incrementAmount().compareTo(auction.getMinimumIncrement()) < 0) {
                return new Response(false, "Bước auto-bid phải từ " + auction.getMinimumIncrement() + " trở lên.", null);
            }
            if (userDao.findById(request.bidderId()).isEmpty()) {
                return new Response(false, "Bidder không tồn tại.", null);
            }

            LocalDateTime now = LocalDateTime.now();
            AutoBidConfig config = autoBidDao
                    .findByAuctionIdAndBidderId(request.auctionId(), request.bidderId())
                    .orElseGet(() -> new AutoBidConfig(
                            UUID.randomUUID(),
                            now,
                            now,
                            request.auctionId(),
                            request.bidderId(),
                            request.maxBid(),
                            request.incrementAmount(),
                            true
                    ));

            config.setMaxBid(request.maxBid());
            config.setIncrementAmount(request.incrementAmount());
            config.setEnabled(true);
            config.setUpdatedAt(now);
            autoBidDao.save(config);

            return new Response(true, "Đã lưu cấu hình auto-bid.", config);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi cấu hình auto-bid: " + e.getMessage(), null);
        }
    }
}
