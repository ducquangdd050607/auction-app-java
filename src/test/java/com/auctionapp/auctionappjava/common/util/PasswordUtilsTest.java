package com.auctionapp.auctionappjava.common.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PasswordUtils - bao mat mat khau")
class PasswordUtilsTest {

  // ================================================================ generateSalt
  @Test
  @DisplayName("generateSalt phai tra ve chuoi hex 32 ky tu (16 bytes)")
  void generateSalt_shouldReturn32CharHexString() {
    String salt = PasswordUtils.generateSalt();

    assertNotNull(salt, "Salt không được null");
    assertEquals(32, salt.length(), "Salt phải có đúng 32 ký tự hex");
    assertTrue(salt.matches("[0-9a-f]+"), "Salt chỉ được chứa ký tự hex thường");
  }

  @Test
  @DisplayName("generateSalt phai cho ket qua ngau nhien moi lan goi")
  void generateSalt_shouldBeRandom() {
    String salt1 = PasswordUtils.generateSalt();
    String salt2 = PasswordUtils.generateSalt();

    assertNotEquals(
        salt1, salt2, "Hai lần gọi generateSalt phải cho kết quả khác nhau (tính ngẫu nhiên)");
  }

  // ================================================================ hashPassword
  @Test
  @DisplayName("hashPassword cung input phai cho cung output (deterministic)")
  void hashPassword_shouldBeDeterministic() {
    String salt = "fixed-salt-for-test";
    String hash1 = PasswordUtils.hashPassword("myPassword", salt);
    String hash2 = PasswordUtils.hashPassword("myPassword", salt);

    assertEquals(hash1, hash2, "Hash phải ổn định với cùng password và salt");
  }

  @Test
  @DisplayName("hashPassword voi salt khac nhau phai cho ket qua khac (chong rainbow table)")
  void hashPassword_differentSalts_shouldProduceDifferentHashes() {
    String hash1 = PasswordUtils.hashPassword("samePassword", "salt-A");
    String hash2 = PasswordUtils.hashPassword("samePassword", "salt-B");

    assertNotEquals(hash1, hash2, "Cùng password nhưng salt khác → hash phải khác nhau");
  }

  @Test
  @DisplayName("hashPassword voi password khac nhau phai cho ket qua khac")
  void hashPassword_differentPasswords_shouldProduceDifferentHashes() {
    String salt = PasswordUtils.generateSalt();
    String hash1 = PasswordUtils.hashPassword("password-one", salt);
    String hash2 = PasswordUtils.hashPassword("password-two", salt);

    assertNotEquals(hash1, hash2, "Password khác nhau → hash phải khác nhau");
  }

  @Test
  @DisplayName("hashPassword phai tra ve chuoi hex SHA-256 (64 ky tu)")
  void hashPassword_shouldReturn64CharSha256Hex() {
    String hash = PasswordUtils.hashPassword("anyPassword", "anySalt");

    assertNotNull(hash);
    assertEquals(64, hash.length(), "SHA-256 hex phải có 64 ký tự");
    assertTrue(hash.matches("[0-9a-f]+"), "Hash chỉ được chứa ký tự hex thường");
  }

  // ================================================================ verifyPassword
  @Test
  @DisplayName("verifyPassword - happy path: dung mat khau phai tra ve true")
  void verifyPassword_withCorrectCredentials_shouldReturnTrue() {
    String rawPassword = "SuperSecret@123";
    String salt = PasswordUtils.generateSalt();
    String hash = PasswordUtils.hashPassword(rawPassword, salt);

    assertTrue(
        PasswordUtils.verifyPassword(rawPassword, salt, hash),
        "Mật khẩu đúng phải được xác thực thành công");
  }

  @Test
  @DisplayName("verifyPassword - sai mat khau phai tra ve false")
  void verifyPassword_withWrongPassword_shouldReturnFalse() {
    String salt = PasswordUtils.generateSalt();
    String hash = PasswordUtils.hashPassword("correctPassword", salt);

    assertFalse(
        PasswordUtils.verifyPassword("wrongPassword", salt, hash), "Mật khẩu sai phải bị từ chối");
  }

  @Test
  @DisplayName("verifyPassword - sai salt phai tra ve false")
  void verifyPassword_withWrongSalt_shouldReturnFalse() {
    String correctSalt = PasswordUtils.generateSalt();
    String hash = PasswordUtils.hashPassword("password", correctSalt);

    assertFalse(
        PasswordUtils.verifyPassword("password", "wrong-salt", hash),
        "Salt sai phải bị từ chối dù password đúng");
  }

  @Test
  @DisplayName("verifyPassword - password null phai tra ve false (khong crash)")
  void verifyPassword_withNullPassword_shouldReturnFalse() {
    assertFalse(PasswordUtils.verifyPassword(null, "salt", "hash"));
  }

  @Test
  @DisplayName("verifyPassword - salt null phai tra ve false (khong crash)")
  void verifyPassword_withNullSalt_shouldReturnFalse() {
    assertFalse(PasswordUtils.verifyPassword("password", null, "hash"));
  }

  @Test
  @DisplayName("verifyPassword - hash null phai tra ve false (khong crash)")
  void verifyPassword_withNullHash_shouldReturnFalse() {
    assertFalse(PasswordUtils.verifyPassword("password", "salt", null));
  }

  @Test
  @DisplayName("verifyPassword - toan bo input null phai tra ve false")
  void verifyPassword_withAllNull_shouldReturnFalse() {
    assertFalse(PasswordUtils.verifyPassword(null, null, null));
  }
}
