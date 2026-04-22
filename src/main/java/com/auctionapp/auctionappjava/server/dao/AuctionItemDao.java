package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.Item;

import java.util.*;

public interface AuctionItemDao {
    Item save(Item item);

    Optional<Item> findById(UUID id);

    void update(Item item);

    void delete(UUID id);
}
