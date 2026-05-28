package com.auctionapp.auctionappjava.server.strategy;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.server.model.Auction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("AntiSnipingExtensionStrategy - chong dat gia cuoi gio")
class AntiSnipingExtensionStrategyTest {

  // Cấu hình: nếu còn <= 30 giây thì gia hạn thêm 60 giây
  private AntiSnipingExtensionStrategy strategy;

  @BeforeEach
  void setUp() {
    strategy = new AntiSnipingExtensionStrategy(30, 60);
  }

  // ---- Helper tạo Auction tối giản ----
  private Auction makeAuction(LocalDateTime endTime) {
    LocalDateTime start = endTime.minusHours(1);
    return new Auction(
        UUID.randomUUID(),
        start,
        start,
        UUID.randomUUID(), // itemId
        UUID.randomUUID(), // sellerId
        new BigDecimal("1000000"),
        null,
        start,
        endTime,
        AuctionStatus.RUNNING,
        new BigDecimal("50000"),
        null);
  }

  // shouldExtend
  @Test
  @DisplayName("shouldExtend - dat gia khi con 10 giay -> phai gia han")
  void shouldExtend_whenBidWithin10Seconds_returnsTrue() {
    LocalDateTime bidTime = LocalDateTime.now();
    LocalDateTime endTime = bidTime.plusSeconds(10); // còn 10s < 30s ngưỡng

    boolean result = strategy.shouldExtend(makeAuction(endTime), bidTime);

    assertTrue(result, "Còn 10 giây trong ngưỡng 30 giây → phải gia hạn");
  }

  @Test
  @DisplayName("shouldExtend - dat gia dung nguong 30 giay -> phai gia han")
  void shouldExtend_atExactThreshold_returnsTrue() {
    LocalDateTime bidTime = LocalDateTime.now();
    LocalDateTime endTime = bidTime.plusSeconds(30); // đúng bằng ngưỡng

    assertTrue(strategy.shouldExtend(makeAuction(endTime), bidTime));
  }

  @Test
  @DisplayName("shouldExtend - dat gia khi con 60 giay -> KHONG gia han")
  void shouldExtend_whenBidFarFromEnd_returnsFalse() {
    LocalDateTime bidTime = LocalDateTime.now();
    LocalDateTime endTime = bidTime.plusSeconds(60); // còn 60s > 30s ngưỡng

    boolean result = strategy.shouldExtend(makeAuction(endTime), bidTime);

    assertFalse(result, "Còn 60 giây vượt ngưỡng 30 giây → không gia hạn");
  }

  @Test
  @DisplayName("shouldExtend - dat gia khi da het gio (am) -> KHONG gia han")
  void shouldExtend_whenAuctionAlreadyEnded_returnsFalse() {
    LocalDateTime bidTime = LocalDateTime.now();
    LocalDateTime endTime = bidTime.minusSeconds(5); // đã kết thúc 5 giây trước

    assertFalse(strategy.shouldExtend(makeAuction(endTime), bidTime));
  }

  // ================================================================ extendTo
  @Test
  @DisplayName("extendTo - khi can gia han, endTime phai tang them dung extensionSeconds")
  void extendTo_shouldAddExtensionSecondsToEndTime() {
    LocalDateTime bidTime = LocalDateTime.now();
    LocalDateTime endTime = bidTime.plusSeconds(10); // còn 10s → gia hạn
    Auction auction = makeAuction(endTime);

    LocalDateTime newEnd = strategy.extendTo(auction, bidTime);

    assertEquals(endTime.plusSeconds(60), newEnd, "endTime mới phải = endTime cũ + 60 giây");
  }

  @Test
  @DisplayName("extendTo - khi KHONG can gia han, phai tra ve endTime goc")
  void extendTo_whenShouldNotExtend_returnsOriginalEndTime() {
    LocalDateTime bidTime = LocalDateTime.now();
    LocalDateTime endTime = bidTime.plusSeconds(120); // còn 120s → không gia hạn
    Auction auction = makeAuction(endTime);

    LocalDateTime newEnd = strategy.extendTo(auction, bidTime);

    assertEquals(endTime, newEnd, "Không cần gia hạn → endTime phải không đổi");
  }

  @Test
  @DisplayName("extendTo - kiem tra gia han voi nguong tuy chinh (60s/120s)")
  void extendTo_withCustomConfig_shouldRespectCustomExtension() {
    // Tạo strategy với config khác: ngưỡng 60s, gia hạn 120s
    AntiSnipingExtensionStrategy customStrategy = new AntiSnipingExtensionStrategy(60, 120);

    LocalDateTime bidTime = LocalDateTime.now();
    LocalDateTime endTime = bidTime.plusSeconds(30); // 30s < ngưỡng 60s → gia hạn
    Auction auction = makeAuction(endTime);

    LocalDateTime newEnd = customStrategy.extendTo(auction, bidTime);

    assertEquals(endTime.plusSeconds(120), newEnd, "Strategy tùy chỉnh phải gia hạn đúng 120 giây");
  }

  @Test
  @DisplayName("shouldExtend va extendTo phai nhat quan voi nhau")
  void shouldExtend_andExtendTo_mustBeConsistent() {
    LocalDateTime bidTime = LocalDateTime.now();
    LocalDateTime endTime = bidTime.plusSeconds(5);
    Auction auction = makeAuction(endTime);

    // Nếu shouldExtend = true thì extendTo phải thay đổi endTime
    if (strategy.shouldExtend(auction, bidTime)) {
      assertNotEquals(
          endTime,
          strategy.extendTo(auction, bidTime),
          "Nếu shouldExtend = true thì extendTo phải cho kết quả khác endTime gốc");
    } else {
      assertEquals(
          endTime,
          strategy.extendTo(auction, bidTime),
          "Nếu shouldExtend = false thì extendTo phải trả về endTime gốc");
    }
  }
}
