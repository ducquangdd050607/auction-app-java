package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutoBidDao {
    AutoBidConfig save(AutoBidConfig config); // lưu 1 autobid

    Optional<AutoBidConfig> findByAuctionIdAndBidderId(UUID auctionId, UUID bidderId); // in ra thong tin của autobid khi biết id phiên và bidder

    List<AutoBidConfig> findEnabledByAuctionId(UUID auctionId); // in ra thông tin của những cấu hình auto_bid đang bật

    void deleteByAuctionId(UUID auctionId); // xóa cấu hình autobid nếu phiên bị hủy
}
