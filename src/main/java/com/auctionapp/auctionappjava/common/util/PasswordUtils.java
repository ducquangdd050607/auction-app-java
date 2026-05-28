package com.auctionapp.auctionappjava.common.util;

// Bảo mật mật khẩu người dùng
// Không lưu password dạng plain text
import com.auctionapp.auctionappjava.common.exception.AppException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

public final class PasswordUtils {

  private static final SecureRandom R = new SecureRandom();

  private PasswordUtils() {}

  public static String generateSalt() {
    byte[] b = new byte[16];
    R.nextBytes(b);
    return HexFormat.of().formatHex(b);
  }

  public static String hashPassword(String raw, String salt) {
    try {
      MessageDigest d = MessageDigest.getInstance("SHA-256");
      return HexFormat.of()
          .formatHex(d.digest((salt + ":" + raw).getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new AppException("Không thể khởi tạo thuật toán băm mật khẩu", e);
    }
  }

  public static boolean verifyPassword(String raw, String salt, String hash) {
    return raw != null
        && salt != null
        && hash != null
        && MessageDigest.isEqual(
            hashPassword(raw, salt).getBytes(StandardCharsets.UTF_8),
            hash.getBytes(StandardCharsets.UTF_8));
  }
}
