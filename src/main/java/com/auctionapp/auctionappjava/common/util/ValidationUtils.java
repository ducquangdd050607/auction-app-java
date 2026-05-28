package com.auctionapp.auctionappjava.common.util;

import com.auctionapp.auctionappjava.common.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

// Validate dữ liệu đầu vào (input validation)
// Đảm bảo dữ liệu hợp lệ trước khi xử lý business logic
// Nếu sai → ném ValidationException
public final class ValidationUtils {

  private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  private ValidationUtils() {}

  public static String requireText(String type, String place) {
    if (type == null || type.trim().isEmpty()) {
      throw new ValidationException(place + " khong duoc bo trong");
    }
    return type.trim();
  }

  public static void requireEmail(String email) {
    if (email == null || !EMAIL.matcher(email.trim()).matches()) {
      throw new ValidationException("Email khong hop le");
    }
  }

  public static void requireConfirmPassword(String oldPassword, String newPassword) {
    if (!oldPassword.equals(newPassword)) {
      throw new ValidationException("Mat khau khong khop");
    }
  }

  public static void requireRole(String role) {
    if (role == null) {
      throw new ValidationException("Xin hay chon vai tro");
    }
  }

  public static void requirePositive(BigDecimal v, String f) {
    if (v == null || v.compareTo(BigDecimal.ZERO) <= 0) {
      throw new ValidationException(f + " phai lon hon 0");
    }
  }

  public static void requireTimeRange(LocalDateTime s, LocalDateTime e) {
    if (s == null || e == null || !e.isAfter(s)) {
      throw new ValidationException("Thoi gian ket thuc phai sau thoi gian bat dau");
    }
  }

  public static void requireUnderMaximumLetters(String string) {
    if (string.length() == 90) {
      throw new ValidationException("Gioi han toi da 90 ki tu");
    }
  }

  public static void requireValidAutoStep(BigDecimal autoStep, BigDecimal minIncrement) {
    if (autoStep == null || minIncrement == null || autoStep.compareTo(minIncrement) < 0) {
      throw new ValidationException("Buoc tien tu dat cuoc dang nho hon buoc dat");
    }
  }

  // Kiểm tra số tiền tự đặt cược tối đa so với giá đặt hiện tại
  public static void requireValidAutoMax(BigDecimal maxAuto, BigDecimal currentPrice) {
    if (maxAuto == null || currentPrice == null || maxAuto.compareTo(currentPrice) < 0) {
      throw new ValidationException("Tien tu dat cuoc toi da dang nho hon gia hien tai");
    }
  }

  // Kiểm tra tiền cược mới so với giá cao nhất hiện tại
  public static void requireGreaterThanBest(BigDecimal bidPrice, BigDecimal bestPrice) {
    if (bidPrice == null || bestPrice == null || bidPrice.compareTo(bestPrice) <= 0) {
      throw new ValidationException("Tien cuoc dang nho hon hien tai!");
    }
  }

  // Kiểm tra khoảng cách tăng giá phải đạt tối thiểu bước giá quy định
  public static void requireValidIncrement(
      BigDecimal bidPrice, BigDecimal bestPrice, BigDecimal minIncrement) {
    if (bidPrice == null
        || bestPrice == null
        || minIncrement == null
        || bidPrice.subtract(bestPrice).compareTo(minIncrement) < 0) {
      throw new ValidationException("Vui long nhieu hon muc quy dinh.");
    }
  }
}
