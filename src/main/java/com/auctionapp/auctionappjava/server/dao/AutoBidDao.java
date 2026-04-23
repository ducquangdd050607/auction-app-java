package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutoBidDao {
    void save(AutoBidConfig config);
    void update(AutoBidConfig config);
    void upsert(AutoBidConfig config);
    Optional<AutoBidConfig> findByAuctionAndBidder(UUID auctionId, UUID bidderId);
    List<AutoBidConfig> findEnabledByAuctionId(UUID auctionId);
    List<AutoBidConfig> findByBidderId(UUID bidderId);
    void setEnabled(UUID auctionId, UUID bidderId, boolean enabled);
    void delete(UUID configId);
}
