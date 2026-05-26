package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.server.model.Item;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionItemDao {
    Item save(Item item);

    Optional<Item> findById(UUID itemId);

    Optional<byte[]> findImageByAuctionId(UUID auctionId);

    Optional<Item> findByIdWithoutImage(UUID itemId);

    List<Item> findBySellerId(UUID sellerId);

    Optional<Item> findByAuctionId(UUID auctionId);

    Optional<Item> findByAuctionIdWithoutImage(UUID auctionId);

    void deleteById(UUID itemId);

    void nookzzAll();
}
