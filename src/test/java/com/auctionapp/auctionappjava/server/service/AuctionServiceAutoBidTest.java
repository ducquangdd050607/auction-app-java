package com.auctionapp.auctionappjava.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.dto.ConfigureAutoBidRequest;
import com.auctionapp.auctionappjava.common.dto.PlaceBidRequest;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.server.model.Auction;
import com.auctionapp.auctionappjava.server.model.AutoBidConfig;
import com.auctionapp.auctionappjava.server.model.Wallet;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AuctionServiceAutoBidTest {

  private AuctionService auctionService;
  private TestDaoFakes.FakeAuctionDao fakeAuctionDao;
  private TestDaoFakes.FakeItemDao fakeItemDao;
  private TestDaoFakes.FakeBidDao fakeBidDao;
  private TestDaoFakes.FakeAutoBidDao fakeAutoBidDao;
  private TestDaoFakes.FakeUserDao fakeUserDao;

  @BeforeEach
  public void setUp() throws Exception {
    auctionService = new AuctionService();

    fakeAuctionDao = new TestDaoFakes.FakeAuctionDao();
    fakeItemDao = new TestDaoFakes.FakeItemDao();
    fakeBidDao = new TestDaoFakes.FakeBidDao();
    fakeAutoBidDao = new TestDaoFakes.FakeAutoBidDao();
    fakeUserDao = new TestDaoFakes.FakeUserDao();

    setPrivateField("auctionDao", fakeAuctionDao);
    setPrivateField("itemDao", fakeItemDao);
    setPrivateField("bidDao", fakeBidDao);
    setPrivateField("autoBidDao", fakeAutoBidDao);
    setPrivateField("userDao", fakeUserDao);
    setPrivateField("notificationDao", new TestDaoFakes.FakeNotificationDao());
  }

  @Test
  public void testConfigureAutoBid_successful() {
    // Test nay kiem tra API luu cau hinh auto-bid:
    // maxBid >= gia hien tai va incrementAmount >= buoc gia toi thieu thi duoc luu.
    UUID auctionId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();

    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));
    fakeUserDao.putWallet(wallet(bidderId, "300"));

    ConfigureAutoBidRequest request =
        new ConfigureAutoBidRequest(
            auctionId, bidderId, new BigDecimal("300"), new BigDecimal("20"), true);

    var response = auctionService.handleConfigureAutoBid(request);

    assertTrue(response.success());
    AutoBidConfig saved =
        fakeAutoBidDao.findByAuctionIdAndBidderId(auctionId, bidderId).orElseThrow();
    assertEquals(new BigDecimal("300"), saved.getMaxBid());
    assertEquals(new BigDecimal("20"), saved.getIncrementAmount());
    assertTrue(saved.isEnabled());
  }

  @Test
  public void testConfigureAutoBid_incrementTooSmall() {
    // Test nay kiem tra validate buoc gia:
    // auction yeu cau buoc toi thieu 10, user cau hinh auto-bid buoc 5 nen phai bi tu choi.
    UUID auctionId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();

    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));

    ConfigureAutoBidRequest request =
        new ConfigureAutoBidRequest(
            auctionId, bidderId, new BigDecimal("300"), new BigDecimal("5"), true);

    var response = auctionService.handleConfigureAutoBid(request);

    assertFalse(response.success());
    assertTrue(fakeAutoBidDao.findByAuctionIdAndBidderId(auctionId, bidderId).isEmpty());
  }

  @Test
  public void handleConfigureAutoBid_missingAuction_shouldReturnFailure() {
    // Technique: EP
    UUID auctionId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();

    var response =
        auctionService.handleConfigureAutoBid(
            new ConfigureAutoBidRequest(
                auctionId, bidderId, new BigDecimal("300"), new BigDecimal("10"), true));

    assertFalse(response.success());
    assertTrue(fakeAutoBidDao.findByAuctionIdAndBidderId(auctionId, bidderId).isEmpty());
  }

  @Test
  public void handleConfigureAutoBid_maxBidBelowCurrentPrice_shouldReturnFailure() {
    // Technique: BVA
    UUID auctionId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();
    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));

    var response =
        auctionService.handleConfigureAutoBid(
            new ConfigureAutoBidRequest(
                auctionId, bidderId, new BigDecimal("99"), new BigDecimal("10"), true));

    assertFalse(response.success());
  }

  @Test
  public void handleConfigureAutoBid_maxBidEqualsCurrentPrice_shouldSucceed() {
    // Technique: BVA
    UUID auctionId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();
    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));
    fakeUserDao.putWallet(wallet(bidderId, "100"));

    var response =
        auctionService.handleConfigureAutoBid(
            new ConfigureAutoBidRequest(
                auctionId, bidderId, new BigDecimal("100"), new BigDecimal("10"), true));

    assertTrue(response.success());
  }

  @Test
  public void handleConfigureAutoBid_incrementEqualsMinimum_shouldSucceed() {
    // Technique: BVA
    UUID auctionId = UUID.randomUUID();
    UUID bidderId = UUID.randomUUID();
    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));
    fakeUserDao.putWallet(wallet(bidderId, "200"));

    var response =
        auctionService.handleConfigureAutoBid(
            new ConfigureAutoBidRequest(
                auctionId, bidderId, new BigDecimal("200"), new BigDecimal("10"), true));

    assertTrue(response.success());
  }

  @Test
  public void handlePlaceBid_autoBidDisabled_shouldNotTriggerAutoBid() {
    // Technique: EP
    UUID auctionId = UUID.randomUUID();
    UUID manualBidderId = UUID.randomUUID();
    UUID autoBidderId = UUID.randomUUID();

    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));
    fakeUserDao.putWallet(wallet(manualBidderId, "1000"));
    fakeUserDao.putWallet(wallet(autoBidderId, "1000"));
    AutoBidConfig disabled = autoBidConfig(auctionId, autoBidderId, "200", "10");
    disabled.setEnabled(false);
    fakeAutoBidDao.save(disabled);

    var response =
        auctionService.handlePlaceBid(
            new PlaceBidRequest(auctionId, manualBidderId, new BigDecimal("120")));

    assertTrue(response.success());
    assertEquals(
        manualBidderId, fakeAuctionDao.findById(auctionId).orElseThrow().getLeadingBidderId());
    assertFalse(
        fakeBidDao.hasSavedAutoBidForAuction(auctionId, autoBidderId, new BigDecimal("130")));
  }

  @Test
  public void handlePlaceBid_noAutoBidCompetitor_shouldNotCreateAutoBid() {
    // Technique: EP
    UUID auctionId = UUID.randomUUID();
    UUID manualBidderId = UUID.randomUUID();

    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));
    fakeUserDao.putWallet(wallet(manualBidderId, "1000"));

    var response =
        auctionService.handlePlaceBid(
            new PlaceBidRequest(auctionId, manualBidderId, new BigDecimal("120")));

    assertTrue(response.success());
    assertEquals(1, fakeBidDao.findByAuctionId(auctionId).size());
  }

  @Test
  public void handlePlaceBid_twoAutoBiddersDifferentMax_shouldHighestMaxLead() {
    // Technique: EP
    UUID auctionId = UUID.randomUUID();
    UUID manualBidderId = UUID.randomUUID();
    UUID lowerAutoBidder = UUID.randomUUID();
    UUID higherAutoBidder = UUID.randomUUID();

    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));
    fakeUserDao.putWallet(wallet(manualBidderId, "1000"));
    fakeUserDao.putWallet(wallet(lowerAutoBidder, "1000"));
    fakeUserDao.putWallet(wallet(higherAutoBidder, "1000"));
    fakeAutoBidDao.save(autoBidConfig(auctionId, lowerAutoBidder, "180", "10"));
    fakeAutoBidDao.save(autoBidConfig(auctionId, higherAutoBidder, "250", "10"));

    var response =
        auctionService.handlePlaceBid(
            new PlaceBidRequest(auctionId, manualBidderId, new BigDecimal("120")));

    assertTrue(response.success());
    assertEquals(
        higherAutoBidder, fakeAuctionDao.findById(auctionId).orElseThrow().getLeadingBidderId());
  }

  @Test
  public void testPlaceBid_triggersAutoBid() {
    // Test nay kiem tra luong tich hop:
    // 1. Bidder A dat tay 120.
    // 2. Bidder B da bat auto-bid max 200, increment 10.
    // 3. He thong tu tao bid 130 cho B, hoan tien A va tru tien B.
    UUID auctionId = UUID.randomUUID();
    UUID manualBidderId = UUID.randomUUID();
    UUID autoBidderId = UUID.randomUUID();

    fakeAuctionDao.save(runningAuction(auctionId, new BigDecimal("100"), null));
    fakeUserDao.putWallet(wallet(manualBidderId, "1000"));
    fakeUserDao.putWallet(wallet(autoBidderId, "1000"));
    fakeAutoBidDao.save(autoBidConfig(auctionId, autoBidderId, "200", "10"));

    var response =
        auctionService.handlePlaceBid(
            new PlaceBidRequest(auctionId, manualBidderId, new BigDecimal("120")));

    assertTrue(response.success());

    Auction auction = fakeAuctionDao.findById(auctionId).orElseThrow();
    assertEquals(new BigDecimal("130"), auction.getCurrentPrice());
    assertEquals(autoBidderId, auction.getLeadingBidderId());
    assertTrue(fakeBidDao.hasSavedBidForAuction(auctionId, new BigDecimal("120")));
    assertTrue(
        fakeBidDao.hasSavedAutoBidForAuction(auctionId, autoBidderId, new BigDecimal("130")));
    assertEquals(
        new BigDecimal("1000"),
        fakeUserDao.findWalletByUserId(manualBidderId).orElseThrow().getBalance());
    assertEquals(
        new BigDecimal("870"),
        fakeUserDao.findWalletByUserId(autoBidderId).orElseThrow().getBalance());
  }

  private void setPrivateField(String fieldName, Object value) throws Exception {
    Field field = auctionService.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(auctionService, value);
  }

  private Auction runningAuction(UUID auctionId, BigDecimal currentPrice, UUID leadingBidderId) {
    return new Auction(
        auctionId,
        LocalDateTime.now().minusMinutes(10),
        LocalDateTime.now().minusMinutes(10),
        UUID.randomUUID(),
        UUID.randomUUID(),
        currentPrice,
        leadingBidderId,
        LocalDateTime.now().minusMinutes(5),
        LocalDateTime.now().plusMinutes(20),
        AuctionStatus.RUNNING,
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

  private AutoBidConfig autoBidConfig(
      UUID auctionId, UUID bidderId, String maxBid, String increment) {
    return new AutoBidConfig(
        UUID.randomUUID(),
        LocalDateTime.now(),
        LocalDateTime.now(),
        auctionId,
        bidderId,
        new BigDecimal(maxBid),
        new BigDecimal(increment),
        true);
  }
}
