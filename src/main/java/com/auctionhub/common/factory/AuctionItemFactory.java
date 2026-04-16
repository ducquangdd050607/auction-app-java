package com.auctionhub.common.factory;

import com.auctionhub.common.dto.CreateAuctionRequest;
import com.auctionhub.common.enums.ItemType;
import com.auctionhub.common.model.ArtItem;
import com.auctionhub.common.model.AuctionItem;
import com.auctionhub.common.model.ElectronicsItem;
import com.auctionhub.common.model.VehicleItem;

import java.time.LocalDateTime;
import java.util.UUID;

public final class AuctionItemFactory {
    private AuctionItemFactory() {
    }

    public static AuctionItem create(UUID sellerId, CreateAuctionRequest request) {
        return create(UUID.randomUUID(), LocalDateTime.now(), LocalDateTime.now(), sellerId, request.itemType(), request.title(),
                request.description(), request.startingPrice(), request.attributeOne(), request.attributeTwo());
    }

    public static AuctionItem create(UUID id,
                                     LocalDateTime createdAt,
                                     LocalDateTime updatedAt,
                                     UUID sellerId,
                                     ItemType itemType,
                                     String title,
                                     String description,
                                     java.math.BigDecimal startingPrice,
                                     String attributeOne,
                                     String attributeTwo) {
        return switch (itemType) {
            case ELECTRONICS -> new ElectronicsItem(id, createdAt, updatedAt, sellerId, title, description, startingPrice, attributeOne, attributeTwo);
            case ART -> new ArtItem(id, createdAt, updatedAt, sellerId, title, description, startingPrice, attributeOne, attributeTwo);
            case VEHICLE -> new VehicleItem(id, createdAt, updatedAt, sellerId, title, description, startingPrice, attributeOne, attributeTwo);
        };
    }
}
