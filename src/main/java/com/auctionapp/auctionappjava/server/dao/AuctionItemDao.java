package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.Item;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionItemDao {
    void save(Item item);
    void update(Item item);
    void delete(UUID itemId);
    Optional<Item> findById(UUID itemId);
    List<Item> findBySellerId(UUID sellerId);
    List<Item> findAll();
}
