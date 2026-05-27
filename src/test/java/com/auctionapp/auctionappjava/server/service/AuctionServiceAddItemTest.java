package com.auctionapp.auctionappjava.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.dto.AddItemRequest;
import com.auctionapp.auctionappjava.common.dto.Response;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuctionServiceAddItemTest {

  private AuctionService service;
  private AuctionServiceTest.FakeAuctionDao auctionDao;
  private AuctionServiceTest.FakeItemDao itemDao;

  @BeforeEach
  void setUp() throws Exception {
    service = new AuctionService();
    auctionDao = new AuctionServiceTest.FakeAuctionDao();
    itemDao = new AuctionServiceTest.FakeItemDao();
    setPrivateField("auctionDao", auctionDao);
    setPrivateField("itemDao", itemDao);
    setPrivateField("bidDao", new AuctionServiceTest.FakeBidDao());
    setPrivateField("autoBidDao", new AuctionServiceTest.FakeAutoBidDao());
    setPrivateField("userDao", new AuctionServiceTest.FakeUserDao());
    setPrivateField("notificationDao", new AuctionServiceTest.FakeNotificationDao());
  }

  @Test
  void handleAddItem_validArt_shouldCreateAuction() {
    // Technique: EP
    Response response = service.handleAddItem(request("ART", "1", "1", 1));

    assertTrue(response.success());
    assertEquals(1, itemDao.store.size());
    assertEquals(1, auctionDao.store.size());
  }

  @Test
  void handleAddItem_validElectronics_shouldCreateAuction() {
    // Technique: EP
    Response response = service.handleAddItem(request("ELECTRONICS", "1", "1", 1));

    assertTrue(response.success());
    assertEquals(1, itemDao.store.size());
  }

  @Test
  void handleAddItem_validVehicle_shouldCreateAuction() {
    // Technique: EP
    Response response = service.handleAddItem(request("VEHICLE", "1", "1", 1));

    assertTrue(response.success());
    assertEquals(1, itemDao.store.size());
  }

  @Test
  void handleAddItem_invalidItemType_shouldReturnFailure() {
    // Technique: EP
    Response response = service.handleAddItem(request("UNKNOWN", "1", "1", 1));

    assertFalse(response.success());
    assertEquals(0, itemDao.store.size());
  }

  @Test
  void handleAddItem_startPriceZero_shouldReturnFailure() {
    // Technique: BVA
    Response response = service.handleAddItem(request("ART", "0", "1", 1));

    assertFalse(response.success());
  }

  @Test
  void handleAddItem_minIncrementZero_shouldReturnFailure() {
    // Technique: BVA
    Response response = service.handleAddItem(request("ART", "1", "0", 1));

    assertFalse(response.success());
  }

  @Test
  void handleAddItem_endTimeEqualsStartTime_shouldReturnFailure() {
    // Technique: BVA
    Response response = service.handleAddItem(request("ART", "1", "1", 0));

    assertFalse(response.success());
  }

  @Test
  void handleAddItem_endTimeBeforeStartTime_shouldReturnFailure() {
    // Technique: BVA
    Response response = service.handleAddItem(request("ART", "1", "1", -1));

    assertFalse(response.success());
  }

  private AddItemRequest request(
      String itemType, String startPrice, String minIncrement, int endOffsetHours) {
    LocalDateTime start = LocalDateTime.now().plusHours(1);
    return new AddItemRequest(
        UUID.randomUUID().toString(),
        "Title",
        "Description",
        new BigDecimal(startPrice),
        new BigDecimal(minIncrement),
        itemType,
        start,
        start.plusHours(endOffsetHours),
        "A",
        "B",
        new byte[] {1});
  }

  private void setPrivateField(String fieldName, Object value) throws Exception {
    Field field = service.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }
}
