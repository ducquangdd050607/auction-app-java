package com.auctionhub.server.dao;

import com.auctionhub.common.model.BidTransaction;

import java.util.List;
import java.util.UUID;

public interface BidDao {
    BidTransaction save(BidTransaction bidTransaction);

    void saveAll(List<BidTransaction> bidTransactions);

    List<BidTransaction> findByAuctionId(UUID auctionId);
}
