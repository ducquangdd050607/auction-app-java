package com.auctionapp.auctionappjava.common.util;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ValidationUtils — kiểm tra dữ liệu đầu vào")
class ValidationUtilsTest {

  // ================================================================ requireText
  @Test
  @DisplayName("requireText — chuỗi hợp lệ phải được trim và trả về")
  void requireText_withValidString_shouldReturnTrimmed() {
    String result = ValidationUtils.requireText("  Nguyễn Văn A  ", "Họ tên");
    assertEquals("Nguyễn Văn A", result, "Chuỗi phải được loại bỏ khoảng trắng đầu cuối");
  }

  @Test
  @DisplayName("requireText — chuỗi không có khoảng trắng thừa phải trả về nguyên")
  void requireText_withNoWhitespace_shouldReturnAsIs() {
    String result = ValidationUtils.requireText("admin", "Tên đăng nhập");
    assertEquals("admin", result);
  }

  @Test
  @DisplayName("requireText — null phải ném ValidationException")
  void requireText_withNull_shouldThrowValidationException() {
    ValidationException ex =
        assertThrows(
            ValidationException.class, () -> ValidationUtils.requireText(null, "Tên đăng nhập"));
    assertTrue(ex.getMessage().contains("Tên đăng nhập"), "Thông báo lỗi phải đề cập tên field");
  }

  @Test
  @DisplayName("requireText — chuỗi rỗng phải ném ValidationException")
  void requireText_withEmpty_shouldThrowValidationException() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireText("", "Email"));
  }

  @Test
  @DisplayName("requireText — chỉ khoảng trắng phải ném ValidationException")
  void requireText_withBlankOnly_shouldThrowValidationException() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireText("   ", "Mô tả"));
  }

  // ================================================================ requireEmail
  @Test
  @DisplayName("requireEmail — email hợp lệ phải không ném exception")
  void requireEmail_withValidEmail_shouldNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.requireEmail("user@example.com"));
  }

  @Test
  @DisplayName("requireEmail — email có subdomain phải hợp lệ")
  void requireEmail_withSubdomain_shouldNotThrow() {
    assertDoesNotThrow(() -> ValidationUtils.requireEmail("test@mail.company.vn"));
  }

  @Test
  @DisplayName("requireEmail — email thiếu @ phải ném ValidationException")
  void requireEmail_withoutAtSign_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireEmail("not-an-email"));
  }

  @Test
  @DisplayName("requireEmail — email thiếu domain phải ném ValidationException")
  void requireEmail_withoutDomain_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireEmail("user@"));
  }

  @Test
  @DisplayName("requireEmail — email null phải ném ValidationException")
  void requireEmail_withNull_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireEmail(null));
  }

  @Test
  @DisplayName("requireEmail — chuỗi rỗng phải ném ValidationException")
  void requireEmail_withEmpty_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requireEmail(""));
  }

  // ================================================================ requirePositive
  @Test
  @DisplayName("requirePositive — số dương hợp lệ không ném exception")
  void requirePositive_withPositiveValue_shouldNotThrow() {
    assertDoesNotThrow(
        () -> ValidationUtils.requirePositive(new BigDecimal("100000"), "Giá khởi điểm"));
  }

  @Test
  @DisplayName("requirePositive — số 0 phải ném ValidationException")
  void requirePositive_withZero_shouldThrow() {
    ValidationException ex =
        assertThrows(
            ValidationException.class,
            () -> ValidationUtils.requirePositive(BigDecimal.ZERO, "Giá khởi điểm"));
    assertTrue(ex.getMessage().contains("Giá khởi điểm"));
  }

  @Test
  @DisplayName("requirePositive — số âm phải ném ValidationException")
  void requirePositive_withNegative_shouldThrow() {
    assertThrows(
        ValidationException.class,
        () -> ValidationUtils.requirePositive(new BigDecimal("-1"), "Bước giá"));
  }

  @Test
  @DisplayName("requirePositive — null phải ném ValidationException")
  void requirePositive_withNull_shouldThrow() {
    assertThrows(ValidationException.class, () -> ValidationUtils.requirePositive(null, "Giá trị"));
  }

  // ================================================================ requireTimeRange
  @Test
  @DisplayName("requireTimeRange — khoảng thời gian hợp lệ (end > start) không ném exception")
  void requireTimeRange_withValidRange_shouldNotThrow() {
    LocalDateTime start = LocalDateTime.now();
    LocalDateTime end = start.plusDays(7);
    assertDoesNotThrow(() -> ValidationUtils.requireTimeRange(start, end));
  }

  @Test
  @DisplayName("requireTimeRange — end trước start phải ném ValidationException")
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
  @DisplayName("requireTimeRange — start null phải ném ValidationException")
  void requireTimeRange_withNullStart_shouldThrow() {
    assertThrows(
        ValidationException.class,
        () -> ValidationUtils.requireTimeRange(null, LocalDateTime.now()));
  }

  @Test
  @DisplayName("requireTimeRange — end null phải ném ValidationException")
  void requireTimeRange_withNullEnd_shouldThrow() {
    assertThrows(
        ValidationException.class,
        () -> ValidationUtils.requireTimeRange(LocalDateTime.now(), null));
  }
}
