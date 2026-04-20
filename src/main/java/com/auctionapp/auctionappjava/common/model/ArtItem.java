package com.auctionapp.auctionappjava.common.model;

import com.auctionapp.auctionappjava.common.enums.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ArtItem extends AuctionItem {
    public ArtItem() {
        setItemType(ItemType.ART);
    }

    public ArtItem(UUID id,
                   LocalDateTime createdAt,
                   LocalDateTime updatedAt,
                   UUID sellerId,
                   String title,
                   String description,
                   BigDecimal startingPrice,
                   String artist,
                   String medium) {
        super(id, createdAt, updatedAt, sellerId, title, description, startingPrice, ItemType.ART, artist, medium);
    }
}
