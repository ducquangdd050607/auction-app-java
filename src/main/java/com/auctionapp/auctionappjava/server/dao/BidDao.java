package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.BidTransaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BidDao {
    void save(BidTransaction bid);
    Optional<BidTransaction> findById(UUID bidId);
    List<BidTransaction> findByAuctionId(UUID auctionId);
    List<BidTransaction> findByBidderId(UUID bidderId);
    Optional<BidTransaction> findHighestByAuctionId(UUID auctionId);
    long countByAuctionId(UUID auctionId);
    long countByBidderId(UUID bidderId);
    BigDecimal totalBidVolume();
}
