package com.auctionapp.auctionappjava.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.dto.PlaceBidRequest;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.server.model.Auction;
import com.auctionapp.auctionappjava.server.model.Wallet;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuctionServicePlaceBidTest {

  private AuctionService service;
  private AuctionServiceTest.FakeAuctionDao auctionDao;
  private AuctionServiceTest.FakeBidDao bidDao;
  private AuctionServiceTest.FakeUserDao userDao;
  private UUID auctionId;
  private UUID bidderId;

  @BeforeEach
  void setUp() throws Exception {
    service = new AuctionService();
    auctionDao = new AuctionServiceTest.FakeAuctionDao();
    bidDao = new AuctionServiceTest.FakeBidDao();
    userDao = new AuctionServiceTest.FakeUserDao();
    setPrivateField("auctionDao", auctionDao);
    setPrivateField("itemDao", new AuctionServiceTest.FakeItemDao());
    setPrivateField("bidDao", bidDao);
    setPrivateField("autoBidDao", new AuctionServiceTest.FakeAutoBidDao());
    setPrivateField("userDao", userDao);
    setPrivateField("notificationDao", new AuctionServiceTest.FakeNotificationDao());
    auctionId = UUID.randomUUID();
    bidderId = UUID.randomUUID();
  }

  @Test
  void handlePlaceBid_amountAboveMinimum_shouldSucceed() {
    // Technique: EP
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));
    userDao.putWallet(wallet(bidderId, "500"));

    Response response = place("120");

    assertTrue(response.success());
    assertEquals(
        new BigDecimal("120"), auctionDao.findById(auctionId).orElseThrow().getCurrentPrice());
  }

  @Test
  void handlePlaceBid_missingAuction_shouldReturnFailure() {
    // Technique: EP
    userDao.putWallet(wallet(bidderId, "500"));

    Response response = place("120");

    assertFalse(response.success());
    assertEquals(0, bidDao.findByAuctionId(auctionId).size());
  }

  @Test
  void handlePlaceBid_finishedAuction_shouldReturnFailure() {
    // Technique: EP
    auctionDao.save(auction(AuctionStatus.FINISHED, "100", null));
    userDao.putWallet(wallet(bidderId, "500"));

    Response response = place("120");

    assertFalse(response.success());
  }

  @Test
  void handlePlaceBid_openAuction_shouldSucceed() {
    // Technique: EP
    auctionDao.save(auction(AuctionStatus.OPEN, "100", null));
    userDao.putWallet(wallet(bidderId, "500"));

    Response response = place("120");

    assertTrue(response.success());
  }

  @Test
  void handlePlaceBid_missingWallet_shouldReturnFailure() {
    // Technique: EP
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));

    Response response = place("120");

    assertFalse(response.success());
  }

  @Test
  void handlePlaceBid_outbidsDifferentLeader_shouldRefundOldLeader() {
    // Technique: EP
    UUID oldLeader = UUID.randomUUID();
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", oldLeader));
    userDao.putWallet(wallet(oldLeader, "400"));
    userDao.putWallet(wallet(bidderId, "500"));

    Response response = place("120");

    assertTrue(response.success());
    assertEquals(
        new BigDecimal("500"), userDao.findWalletByUserId(oldLeader).orElseThrow().getBalance());
    assertEquals(
        new BigDecimal("380"), userDao.findWalletByUserId(bidderId).orElseThrow().getBalance());
  }

  @Test
  void handlePlaceBid_sameLeader_shouldUseAvailableBalanceIncludingCurrentBid() {
    // Technique: EP
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", bidderId));
    userDao.putWallet(wallet(bidderId, "20"));

    Response response = place("110");

    assertTrue(response.success());
    assertEquals(
        new BigDecimal("10"), userDao.findWalletByUserId(bidderId).orElseThrow().getBalance());
  }

  @Test
  void handlePlaceBid_amountBelowCurrentPrice_shouldReturnFailure() {
    // Technique: BVA
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));
    userDao.putWallet(wallet(bidderId, "500"));

    assertFalse(place("99").success());
  }

  @Test
  void handlePlaceBid_amountEqualsCurrentPrice_shouldReturnFailure() {
    // Technique: BVA
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));
    userDao.putWallet(wallet(bidderId, "500"));

    assertFalse(place("100").success());
  }

  @Test
  void handlePlaceBid_amountBelowMinimumRequired_shouldReturnFailure() {
    // Technique: BVA
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));
    userDao.putWallet(wallet(bidderId, "500"));

    assertFalse(place("109").success());
  }

  @Test
  void handlePlaceBid_amountEqualsMinimumRequired_shouldSucceed() {
    // Technique: BVA
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));
    userDao.putWallet(wallet(bidderId, "500"));

    assertTrue(place("110").success());
  }

  @Test
  void handlePlaceBid_amountAboveMinimumRequired_shouldSucceed() {
    // Technique: BVA
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));
    userDao.putWallet(wallet(bidderId, "500"));

    assertTrue(place("111").success());
  }

  @Test
  void handlePlaceBid_balanceBelowAmount_shouldReturnFailure() {
    // Technique: BVA
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));
    userDao.putWallet(wallet(bidderId, "119"));

    assertFalse(place("120").success());
  }

  @Test
  void handlePlaceBid_balanceEqualsAmount_shouldSucceed() {
    // Technique: BVA
    auctionDao.save(auction(AuctionStatus.RUNNING, "100", null));
    userDao.putWallet(wallet(bidderId, "120"));

    assertTrue(place("120").success());
  }

  private Response place(String amount) {
    return service.handlePlaceBid(new PlaceBidRequest(auctionId, bidderId, new BigDecimal(amount)));
  }

  private Auction auction(AuctionStatus status, String currentPrice, UUID leaderId) {
    return new Auction(
        auctionId,
        LocalDateTime.now().minusMinutes(10),
        LocalDateTime.now().minusMinutes(10),
        UUID.randomUUID(),
        UUID.randomUUID(),
        new BigDecimal(currentPrice),
        leaderId,
        LocalDateTime.now().minusMinutes(5),
        LocalDateTime.now().plusMinutes(20),
        status,
        new BigDecimal("10"),
        null);
  }

  private Wallet wallet(UUID userId, String balance) {
    return new Wallet(
        UUID.randomUUID(),
        LocalDateTime.now(),
        LocalDateTime.now(),
        userId,
        new BigDecimal(balance));
  }

  private void setPrivateField(String fieldName, Object value) throws Exception {
    Field field = service.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }
}
