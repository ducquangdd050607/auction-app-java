package com.auctionapp.auctionappjava.common.factory;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public final class AuctionItemFactory {

    private AuctionItemFactory() {}

    public static Item create(
            ItemType t,
            UUID id,
            LocalDateTime c,
            LocalDateTime u,
            UUID sellerId,
            String title,
            String desc,
            BigDecimal price,
            String a1,
            String a2
    ) {
        return switch (t) {
            case ELECTRONICS -> new ElectronicsItem(id, c, u, sellerId, title, desc, price, a1, a2);
            case VEHICLE     -> new VehicleItem(id, c, u, sellerId, title, desc, price, a1, a2);
            case ART         -> new ArtItem(id, c, u, sellerId, title, desc, price, a1, a2);
        };
    }
}