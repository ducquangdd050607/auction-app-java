package com.auctionhub.server.dao;

import com.auctionhub.common.model.AuctionItem;

import java.util.Optional;
import java.util.UUID;

public interface AuctionItemDao {
    AuctionItem save(AuctionItem item);

    AuctionItem update(AuctionItem item);

    Optional<AuctionItem> findById(UUID id);

    void deleteById(UUID id);

    long count();
}
