package com.auctionhub.common.model;

import com.auctionhub.common.enums.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ElectronicsItem extends AuctionItem {
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
