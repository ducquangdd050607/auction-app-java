package com.auctionapp.auctionappjava.server.factory;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.server.model.ArtItem;
import com.auctionapp.auctionappjava.server.model.ElectronicsItem;
import com.auctionapp.auctionappjava.server.model.Item;
import com.auctionapp.auctionappjava.server.model.VehicleItem;
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
      String a2,
      byte[] imageData) {
    return switch (t) {
      case ELECTRONICS ->
          new ElectronicsItem(id, c, u, sellerId, title, desc, price, a1, a2, imageData);
      case VEHICLE -> new VehicleItem(id, c, u, sellerId, title, desc, price, a1, a2, imageData);
      case ART -> new ArtItem(id, c, u, sellerId, title, desc, price, a1, a2, imageData);
    };
  }
}
