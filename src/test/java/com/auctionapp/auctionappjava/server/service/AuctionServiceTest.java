package com.auctionapp.auctionappjava.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.dto.RemoveAuctionRequest;
import com.auctionapp.auctionappjava.common.dto.Response;
import org.junit.jupiter.api.Test;

class AuctionServiceTest {

  @Test
  void handleRemoveAuction_whenRequestIsNull_shouldReturnInvalidRequestMessage() {
    AuctionService auctionService = new AuctionService();

    Response response = auctionService.handleRemoveAuction(null);

    assertFalse(response.success());
    assertEquals("Yêu cầu xóa phiên đấu giá không hợp lệ.", response.message());
    assertNull(response.data());
  }

  @Test
  void handleRemoveAuction_whenUserIdMissing_shouldReturnMissingUserId() {
    AuctionService auctionService = new AuctionService();

    RemoveAuctionRequest request =
        new RemoveAuctionRequest(null, "11111111-1111-1111-1111-111111111111");

    Response response = auctionService.handleRemoveAuction(request);

    assertFalse(response.success());
    assertEquals("Thiếu userId.", response.message());
  }

  @Test
  void handleRemoveAuction_whenAuctionIdMissing_shouldReturnMissingAuctionId() {
    AuctionService auctionService = new AuctionService();

    RemoveAuctionRequest request =
        new RemoveAuctionRequest("11111111-1111-1111-1111-111111111111", null);

    Response response = auctionService.handleRemoveAuction(request);

    assertFalse(response.success());
    assertEquals("Thiếu auctionId.", response.message());
  }
}
