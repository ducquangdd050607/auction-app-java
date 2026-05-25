package com.auctionapp.auctionappjava.common.factory;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.server.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.server.model.ArtItem;
import com.auctionapp.auctionappjava.server.model.ElectronicsItem;
import com.auctionapp.auctionappjava.server.model.Item;
import com.auctionapp.auctionappjava.server.model.VehicleItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("AuctionItemFactory creates the correct Item type")
class AuctionItemFactoryTest {

    private final UUID id = UUID.randomUUID();
    private final UUID sellerId = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();
    private final BigDecimal startPrice = new BigDecimal("500000");
    private final byte[] imageData = new byte[] {1, 2, 3, 4};

    private Item create(ItemType type) {
        return AuctionItemFactory.create(
                type,
                id,
                now,
                now,
                sellerId,
                "Product name",
                "Description",
                startPrice,
                "Attr1",
                "Attr2",
                imageData
        );
    }

    @Test
    @DisplayName("create(ART) returns ArtItem")
    void create_art_returnsArtItem() {
        Item item = create(ItemType.ART);
        assertInstanceOf(ArtItem.class, item);
    }

    @Test
    @DisplayName("create(ELECTRONICS) returns ElectronicsItem")
    void create_electronics_returnsElectronicsItem() {
        Item item = create(ItemType.ELECTRONICS);
        assertInstanceOf(ElectronicsItem.class, item);
    }

    @Test
    @DisplayName("create(VEHICLE) returns VehicleItem")
    void create_vehicle_returnsVehicleItem() {
        Item item = create(ItemType.VEHICLE);
        assertInstanceOf(VehicleItem.class, item);
    }

    @Test
    @DisplayName("Created Item preserves all provided fields")
    void create_shouldPreserveAllFields() {
        Item item = create(ItemType.ELECTRONICS);

        assertEquals(id, item.getId());
        assertEquals(sellerId, item.getSellerId());
        assertEquals("Product name", item.getTitle());
        assertEquals(startPrice, item.getStartingPrice());
        assertEquals(ItemType.ELECTRONICS, item.getItemType());
        assertArrayEquals(imageData, item.getImageData());
    }

    @Test
    @DisplayName("Different UUID values produce distinct items")
    void create_withDifferentIds_producesDistinctItems() {
        Item item1 = AuctionItemFactory.create(
                ItemType.ART,
                UUID.randomUUID(),
                now,
                now,
                sellerId,
                "T1",
                "D1",
                startPrice,
                "",
                "",
                imageData
        );
        Item item2 = AuctionItemFactory.create(
                ItemType.ART,
                UUID.randomUUID(),
                now,
                now,
                sellerId,
                "T2",
                "D2",
                startPrice,
                "",
                "",
                imageData
        );

        assertNotEquals(item1.getId(), item2.getId());
        assertNotEquals(item1.getTitle(), item2.getTitle());
    }
}
