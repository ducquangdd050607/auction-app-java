package com.auctionhub.server.dao;

import com.auctionhub.common.model.AutoBidConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutoBidDao {
    AutoBidConfig upsert(AutoBidConfig config);

    Optional<AutoBidConfig> findByAuctionAndBidder(UUID auctionId, UUID bidderId);

    List<AutoBidConfig> findByAuctionId(UUID auctionId);

    void disableByAuctionId(UUID auctionId);
}
