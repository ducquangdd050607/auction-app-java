package com.auctionapp.auctionappjava.server.model;

import com.auctionapp.auctionappjava.common.enums.ItemType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class VehicleItem extends Item {
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
                       String registrationHint,
                       byte[] imageData) {
        super(id, createdAt, updatedAt, sellerId, title, description, startingPrice, ItemType.VEHICLE, manufacturer, registrationHint,imageData);
    }
}
