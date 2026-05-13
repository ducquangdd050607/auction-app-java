package com.auctionapp.auctionappjava.common.factory;

import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.model.ArtItem;
import com.auctionapp.auctionappjava.common.model.ElectronicsItem;
import com.auctionapp.auctionappjava.common.model.Item;
import com.auctionapp.auctionappjava.common.model.VehicleItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuctionItemFactory — tạo đúng loại Item")
class AuctionItemFactoryTest {

    // Dữ liệu mẫu dùng chung
    private final UUID       id          = UUID.randomUUID();
    private final UUID       sellerId    = UUID.randomUUID();
    private final LocalDateTime now      = LocalDateTime.now();
    private final BigDecimal startPrice  = new BigDecimal("500000");

    private Item create(ItemType type) {
        return AuctionItemFactory.create(type, id, now, now, sellerId,
                "Tên sản phẩm", "Mô tả", startPrice, "Attr1", "Attr2");
    }

    @Test
    @DisplayName("create(ART) phải trả về instance ArtItem")
    void create_art_returnsArtItem() {
        Item item = create(ItemType.ART);
        assertInstanceOf(ArtItem.class, item);
    }

    @Test
    @DisplayName("create(ELECTRONICS) phải trả về instance ElectronicsItem")
    void create_electronics_returnsElectronicsItem() {
        Item item = create(ItemType.ELECTRONICS);
        assertInstanceOf(ElectronicsItem.class, item);
    }

    @Test
    @DisplayName("create(VEHICLE) phải trả về instance VehicleItem")
    void create_vehicle_returnsVehicleItem() {
        Item item = create(ItemType.VEHICLE);
        assertInstanceOf(VehicleItem.class, item);
    }

    @Test
    @DisplayName("Item được tạo ra phải giữ đúng các thuộc tính truyền vào")
    void create_shouldPreserveAllFields() {
        Item item = create(ItemType.ELECTRONICS);

        assertEquals(id,           item.getId());
        assertEquals(sellerId,     item.getSellerId());
        assertEquals("Tên sản phẩm", item.getTitle());
        assertEquals(startPrice,   item.getStartingPrice());
        assertEquals(ItemType.ELECTRONICS, item.getItemType());
    }

    @Test
    @DisplayName("Mỗi lần gọi với UUID khác nhau phải tạo Item khác nhau")
    void create_withDifferentIds_producesDistinctItems() {
        Item item1 = AuctionItemFactory.create(ItemType.ART, UUID.randomUUID(),
                now, now, sellerId, "T1", "D1", startPrice, "", "");
        Item item2 = AuctionItemFactory.create(ItemType.ART, UUID.randomUUID(),
                now, now, sellerId, "T2", "D2", startPrice, "", "");

        assertNotEquals(item1.getId(), item2.getId());
        assertNotEquals(item1.getTitle(), item2.getTitle());
    }
}