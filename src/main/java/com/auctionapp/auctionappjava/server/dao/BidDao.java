package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.BidTransaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BidDao {
    BidTransaction save(BidTransaction bidTransaction); // lưu lại 1 lượt bid

    List<BidTransaction> findByAuctionId(UUID auctionId); // in tất cả lịch sử trả giá trong 1 phiên

    long countByAuctionId(UUID auctionId); // đếm số lượng trả giá trong 1 phiên

    long countByBidderId(UUID bidderId); //đếm số lượng đặt bid của 1 người

    Optional<BidTransaction> findHighestBidByAuctionId(UUID auctionId);

    void deleteByAuctionId(UUID auctionId); // xóa lịch sử đấu giá của 1 phiên
}
