package com.auctionapp.auctionappjava.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MoneyUtils - xu ly chuoi tien te")
class MoneyUtilsTest {

  // ================================================================ purifyingText
  @Test
  @DisplayName("purifyingText - so thuong khong co dau")
  void purifyingText_withPlainNumber_shouldParse() {
    BigDecimal result = MoneyUtils.purifyingText("50000");
    assertEquals(new BigDecimal("50000"), result);
  }

  @Test
  @DisplayName("purifyingText - so co dau cham phan cach kieu VN (1.000.000)")
  void purifyingText_withDotSeparators_shouldStripAndParse() {
    BigDecimal result = MoneyUtils.purifyingText("1.000.000");
    assertEquals(new BigDecimal("1000000"), result);
  }

  @Test
  @DisplayName("purifyingText - so co dau phay phan cach kieu My (1,000,000)")
  void purifyingText_withCommaSeparators_shouldStripAndParse() {
    BigDecimal result = MoneyUtils.purifyingText("1,000,000");
    assertEquals(new BigDecimal("1000000"), result);
  }

  @Test
  @DisplayName("purifyingText - input null phai tra ve ZERO, khong nem exception")
  void purifyingText_withNull_shouldReturnZero() {
    BigDecimal result = MoneyUtils.purifyingText(null);
    assertEquals(BigDecimal.ZERO, result, "null phải trả về BigDecimal.ZERO");
  }

  @Test
  @DisplayName("purifyingText - input rong phai tra ve ZERO")
  void purifyingText_withEmpty_shouldReturnZero() {
    BigDecimal result = MoneyUtils.purifyingText("");
    assertEquals(BigDecimal.ZERO, result);
  }

  @Test
  @DisplayName("purifyingText - chi toan chu cai phai tra ve ZERO")
  void purifyingText_withLettersOnly_shouldReturnZero() {
    BigDecimal result = MoneyUtils.purifyingText("abc");
    assertEquals(BigDecimal.ZERO, result);
  }

  @Test
  @DisplayName("purifyingText - chuoi hon hop chu va so (vi du: '10k') chi lay so")
  void purifyingText_withMixedChars_shouldExtractDigitsOnly() {
    BigDecimal result = MoneyUtils.purifyingText("500.000 VND");
    assertEquals(new BigDecimal("500000"), result);
  }

  @Test
  @DisplayName("purifyingText - chuoi chi khoang trang phai tra ve ZERO")
  void purifyingText_withBlankString_shouldReturnZero() {
    BigDecimal result = MoneyUtils.purifyingText("   ");
    assertEquals(BigDecimal.ZERO, result);
  }

  @Test
  @DisplayName("purifyingText - so 0 phai tra ve ZERO chinh xac")
  void purifyingText_withZero_shouldReturnZero() {
    BigDecimal result = MoneyUtils.purifyingText("0");
    assertEquals(new BigDecimal("0"), result);
  }

  @Test
  @DisplayName("purifyingText - so lon (long max) khong duoc crash")
  void purifyingText_withLargeNumber_shouldHandleWithoutCrash() {
    // Chỉ cần không ném exception
    assertDoesNotThrow(() -> MoneyUtils.purifyingText("999999999999999"));
  }

  // ================================================================ zeroIfNull
  @Test
  @DisplayName("zeroIfNull - voi null phai tra ve BigDecimal.ZERO")
  void zeroIfNull_withNull_shouldReturnZero() {
    assertEquals(BigDecimal.ZERO, MoneyUtils.zeroIfNull(null));
  }

  @Test
  @DisplayName("zeroIfNull - voi gia tri hop le phai tra ve dung gia tri do")
  void zeroIfNull_withValue_shouldReturnSameValue() {
    BigDecimal value = new BigDecimal("999999");
    assertSame(value, MoneyUtils.zeroIfNull(value));
  }

  @Test
  @DisplayName("zeroIfNull - voi ZERO phai tra ve chinh ZERO")
  void zeroIfNull_withZero_shouldReturnZero() {
    assertEquals(BigDecimal.ZERO, MoneyUtils.zeroIfNull(BigDecimal.ZERO));
  }
}
