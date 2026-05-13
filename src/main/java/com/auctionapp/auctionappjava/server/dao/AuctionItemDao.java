package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.Item;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionItemDao {
    Item save(Item item);

    Optional<Item> findById(UUID itemId);

    List<Item> findBySellerId(UUID sellerId);

    Optional<Item> findByAuctionId(UUID auctionId);

    void deleteById(UUID itemId);

    void nookzzAll();
}
