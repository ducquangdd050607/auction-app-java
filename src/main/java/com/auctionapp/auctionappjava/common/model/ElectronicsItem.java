package com.auctionapp.auctionappjava.common.model;

import com.auctionapp.auctionappjava.common.enums.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ElectronicsItem extends Item {
    public ElectronicsItem() {
        setItemType(ItemType.ELECTRONICS);
    }

    public ElectronicsItem(UUID id,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt,
                           UUID sellerId,
                           String title,
                           String description,
                           BigDecimal startingPrice,
                           String brand,
                           String model) {
        super(id, createdAt, updatedAt, sellerId, title, description, startingPrice, ItemType.ELECTRONICS, brand, model);
    }
}
