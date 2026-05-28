package com.auctionapp.auctionappjava.common.util;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ValidationUtils - kiem tra du lieu dau vao")
class ValidationUtilsTest {

  // ================================================================ requireText
  @Test
  @DisplayName("requireText - chuoi hop le phai duoc trim va tra ve")
  void requireText_withValidString_shouldReturnTrimmed() {
    String result = ValidationUtils.requireText("  Nguyễn Văn A  ", "Họ tên");
    assertEquals("Nguyễn Văn A", result, "Chuỗi phải được loại bỏ khoảng trắng đầu cuối");
  }

  @Test
  @DisplayName("requireText - chuoi khong co khoang trang thua phai tra ve nguyen")
  void requireText_withNoWhitespace_shouldReturnAsIs() {
    String result = ValidationUtils.requireText("admin", "Tên đăng nhập");
    assertEquals("admin", result);
  }

  @Test
  @DisplayName("requireText - null phai nem ValidationException")
  void requireText_withNull_shouldThrowValidationException() {
    ValidationException ex =
        assertThrows(
            ValidationException.class, () -> ValidationUtils.requireText(null, "Tên đăng nhập"));
    assertTrue(ex.getMessage().contains("Tên đăng nhập"), "Thông báo lỗi phải đề cập tên field");
  }

  @Test
  @DisplayName("requireText - chuoi rong phai nem ValidationException")
  void requireText_withEmpty_shouldThrowValidationException() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireText("", "Email"));
  }

  @Test
  @DisplayName("requireText - chi khoang trang phai nem ValidationException")
  void requireText_withBlankOnly_shouldThrowValidationException() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireText("   ", "Mô tả"));
  }

  // ================================================================ requireEmail
  @Test
  @DisplayName("requireEmail - email hop le phai khong nem exception")
  void requireEmail_withValidEmail_shouldNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.requireEmail("user@example.com"));
  }

  @Test
  @DisplayName("requireEmail - email co subdomain phai hop le")
  void requireEmail_withSubdomain_shouldNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.requireEmail("test@mail.company.vn"));
  }

  @Test
  @DisplayName("requireEmail - email thieu @ phai nem ValidationException")
  void requireEmail_withoutAtSign_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireEmail("not-an-email"));
  }

  @Test
  @DisplayName("requireEmail - email thieu domain phai nem ValidationException")
  void requireEmail_withoutDomain_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireEmail("user@"));
  }

  @Test
  @DisplayName("requireEmail - email null phai nem ValidationException")
  void requireEmail_withNull_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireEmail(null));
  }

  @Test
  @DisplayName("requireEmail - chuoi rong phai nem ValidationException")
  void requireEmail_withEmpty_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireEmail(""));
  }

  // ================================================================ requirePositive
  @Test
  @DisplayName("requirePositive - so duong hop le khong nem exception")
  void requirePositive_withPositiveValue_shouldNotThrow() {
    assertDoesNotThrow(
        () -> ValidationUtils.requirePositive(new BigDecimal("100000"), "Giá khởi điểm"));
  }

  @Test
  @DisplayName("requirePositive - so 0 phai nem ValidationException")
  void requirePositive_withZero_shouldThrow() {
    ValidationException ex =
        assertThrows(
            ValidationException.class,
            () -> ValidationUtils.requirePositive(BigDecimal.ZERO, "Giá khởi điểm"));
    assertTrue(ex.getMessage().contains("Giá khởi điểm"));
  }

  @Test
  @DisplayName("requirePositive - so am phai nem ValidationException")
  void requirePositive_withNegative_shouldThrow() {
    assertThrows(
        ValidationException.class,
        () -> ValidationUtils.requirePositive(new BigDecimal("-1"), "Bước giá"));
  }

  @Test
  @DisplayName("requirePositive - null phai nem ValidationException")
  void requirePositive_withNull_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requirePositive(null, "Giá trị"));
  }

  // ================================================================ requireTimeRange
  @Test
  @DisplayName("requireTimeRange - khoang thoi gian hop le (end > start) khong nem exception")
  void requireTimeRange_withValidRange_shouldNotThrow() {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusDays(7);
    assertDoesNotThrow(() -> ValidationUtils.requireTimeRange(start, end));
  }

  @Test
  @DisplayName("requireTimeRange - end truoc start phai nem ValidationException")
  void requireTimeRange_withEndBeforeStart_shouldThrow() {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.minusHours(1);
    assertThrows(ValidationException.class, () -> ValidationUtils.requireTimeRange(start, end));
  }

  @Test
  @DisplayName(
      "requireTimeRange — end bằng start phải ném ValidationException (không thể đấu giá 0 giây)")
  void requireTimeRange_withEqualTimes_shouldThrow() {
    LocalDateTime time = LocalDateTime.now();
    assertThrows(ValidationException.class, () -> ValidationUtils.requireTimeRange(time, time));
  }

  @Test
  @DisplayName("requireTimeRange - start null phai nem ValidationException")
  void requireTimeRange_withNullStart_shouldThrow() {
    assertThrows(
        ValidationException.class,
        () -> ValidationUtils.requireTimeRange(null, LocalDateTime.now()));
  }

  @Test
  @DisplayName("requireTimeRange - end null phai nem ValidationException")
  void requireTimeRange_withNullEnd_shouldThrow() {
    assertThrows(
        ValidationException.class,
        () -> ValidationUtils.requireTimeRange(LocalDateTime.now(), null));
  }
}
