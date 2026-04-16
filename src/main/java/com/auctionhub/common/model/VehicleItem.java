package com.auctionhub.common.model;

import com.auctionhub.common.enums.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class VehicleItem extends AuctionItem {
    public VehicleItem() {
        setItemType(ItemType.VEHICLE);
    }

    public VehicleItem(UUID id,
                       LocalDateTime createdAt,
                       LocalDateTime updatedAt,
                       UUID sellerId,
                       String title,
                       String description,
                       BigDecimal startingPrice,
                       String manufacturer,
                       String registrationHint) {
        super(id, createdAt, updatedAt, sellerId, title, description, startingPrice, ItemType.VEHICLE, manufacturer, registrationHint);
    }
}
