package com.auctionapp.auctionappjava.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.dto.ChangeInformationRequest;
import com.auctionapp.auctionappjava.common.dto.ChangePasswordRequest;
import com.auctionapp.auctionappjava.common.dto.DepositRequest;
import com.auctionapp.auctionappjava.common.dto.LoginRequest;
import com.auctionapp.auctionappjava.common.dto.RegisterRequest;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.util.PasswordUtils;
import com.auctionapp.auctionappjava.server.model.Bidder;
import com.auctionapp.auctionappjava.server.model.Seller;
import com.auctionapp.auctionappjava.server.model.User;
import com.auctionapp.auctionappjava.server.model.Wallet;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

  private UserService service;
  private TestDaoFakes.FakeUserDao userDao;

  @BeforeEach
  void setUp() throws Exception {
    service = new UserService();
    userDao = new TestDaoFakes.FakeUserDao();
    setPrivateField("userDao", userDao);
    setPrivateField("notificationDao", new TestDaoFakes.FakeNotificationDao());
  }

  @Test
  void login_validCredentials_shouldReturnSuccess() {
    // Technique: EP
    User user = user("bidder01", "Binh@123456", Role.BIDDER, true);
    userDao.putUser(user);
    userDao.putWallet(wallet(user.getId(), "500"));

    Response response = service.handleLogin(new LoginRequest("bidder01", "Binh@123456"));

    assertTrue(response.success());
    assertNotNull(response.data());
  }

  @Test
  void login_unknownUsername_shouldReturnFailure() {
    // Technique: EP
    Response response = service.handleLogin(new LoginRequest("not_exist", "Binh@123456"));

    assertFalse(response.success());
    assertNull(response.data());
  }

  @Test
  void login_wrongPassword_shouldReturnFailure() {
    // Technique: EP
    User user = user("bidder01", "Binh@123456", Role.BIDDER, true);
    userDao.putUser(user);
    userDao.putWallet(wallet(user.getId(), "500"));

    Response response = service.handleLogin(new LoginRequest("bidder01", "Wrong@123"));

    assertFalse(response.success());
  }

  @Test
  void login_inactiveUser_shouldReturnFailure() {
    // Technique: EP
    User user = user("bidder01", "Binh@123456", Role.BIDDER, false);
    userDao.putUser(user);
    userDao.putWallet(wallet(user.getId(), "500"));

    Response response = service.handleLogin(new LoginRequest("bidder01", "Binh@123456"));

    assertFalse(response.success());
  }

  @Test
  void login_blankUsername_shouldReturnFailure() {
    // Technique: EP
    Response response = service.handleLogin(new LoginRequest(" ", "Binh@123456"));

    assertFalse(response.success());
  }

  @Test
  void login_nullPassword_shouldReturnFailure() {
    // Technique: EP
    User user = user("bidder01", "Binh@123456", Role.BIDDER, true);
    userDao.putUser(user);
    userDao.putWallet(wallet(user.getId(), "500"));

    Response response = service.handleLogin(new LoginRequest("bidder01", null));

    assertFalse(response.success());
  }

  @Test
  void register_validBidder_shouldCreateUserAndWallet() {
    // Technique: EP
    Response response =
        service.handleRegister(
            new RegisterRequest(
                "new_bidder", "Binh@123456", "Binh Nguyen", "binh@example.com", "BIDDER"));

    assertTrue(response.success());
    assertEquals(1, userDao.users.size());
    assertEquals(1, userDao.wallets.size());
  }

  @Test
  void register_validSeller_shouldCreateSeller() {
    // Technique: EP
    Response response =
        service.handleRegister(
            new RegisterRequest(
                "seller01", "Binh@123456", "Seller One", "seller@example.com", "SELLER"));

    assertTrue(response.success());
    assertEquals(Role.SELLER, userDao.users.values().iterator().next().getRole());
  }

  @Test
  void register_duplicateUsername_shouldReturnFailure() {
    // Technique: EP
    userDao.putUser(user("new_bidder", "Binh@123456", Role.BIDDER, true));

    Response response =
        service.handleRegister(
            new RegisterRequest(
                "new_bidder", "Binh@123456", "Binh Nguyen", "binh@example.com", "BIDDER"));

    assertFalse(response.success());
  }

  @Test
  void register_duplicateEmail_shouldReturnFailure() {
    // Technique: EP
    User existing = user("existing", "Binh@123456", Role.BIDDER, true);
    existing.setEmail("binh@example.com");
    userDao.putUser(existing);

    Response response =
        service.handleRegister(
            new RegisterRequest(
                "new_bidder", "Binh@123456", "Binh Nguyen", "binh@example.com", "BIDDER"));

    assertFalse(response.success());
  }

  @Test
  void register_invalidEmail_shouldReturnFailure() {
    // Technique: EP
    Response response =
        service.handleRegister(
            new RegisterRequest("new_bidder", "Binh@123456", "Binh Nguyen", "bad-email", "BIDDER"));

    assertFalse(response.success());
  }

  @Test
  void register_blankUsername_shouldReturnFailure() {
    // Technique: BVA
    Response response =
        service.handleRegister(
            new RegisterRequest(" ", "Binh@123456", "Binh Nguyen", "binh@example.com", "BIDDER"));

    assertFalse(response.success());
  }

  @Test
  void register_invalidRole_shouldReturnFailure() {
    // Technique: EP
    Response response =
        service.handleRegister(
            new RegisterRequest("u01", "Binh@123456", "User", "u01@example.com", "INVALID"));

    assertFalse(response.success());
  }

  @Test
  void changePassword_existingUser_shouldUpdatePassword() {
    // Technique: EP
    User user = user("bidder01", "Old@123456", Role.BIDDER, true);
    userDao.putUser(user);

    Response response =
        service.handleChangePassword(
            new ChangePasswordRequest(user.getId().toString(), "New@123456"));

    assertTrue(response.success());
    assertTrue(
        PasswordUtils.verifyPassword("New@123456", user.getPasswordSalt(), user.getPasswordHash()));
  }

  @Test
  void changePassword_missingUser_shouldReturnFailure() {
    // Technique: EP
    Response response =
        service.handleChangePassword(
            new ChangePasswordRequest(UUID.randomUUID().toString(), "New@123456"));

    assertFalse(response.success());
  }

  @Test
  void updateProfile_validEmail_shouldReturnSuccess() {
    // Technique: EP
    User user = user("bidder01", "Old@123456", Role.BIDDER, true);
    userDao.putUser(user);

    Response response =
        service.handleChangeInformation(
            new ChangeInformationRequest(user.getId().toString(), "New Name", "new@example.com"));

    assertTrue(response.success());
    assertEquals("new@example.com", user.getEmail());
  }

  @Test
  void updateProfile_invalidEmail_shouldReturnFailure() {
    // Technique: EP
    User user = user("bidder01", "Old@123456", Role.BIDDER, true);
    userDao.putUser(user);

    Response response =
        service.handleChangeInformation(
            new ChangeInformationRequest(user.getId().toString(), "New Name", "bad-email"));

    assertFalse(response.success());
  }

  @Test
  void getBalance_walletExists_shouldReturnBalance() {
    // Technique: EP
    UUID userId = UUID.randomUUID();
    userDao.putWallet(wallet(userId, "123.45"));

    Response response = service.handleGetBalance(userId.toString());

    assertTrue(response.success());
    assertEquals(new BigDecimal("123.45"), response.data());
  }

  @Test
  void getBalance_walletMissing_shouldReturnFailure() {
    // Technique: EP
    Response response = service.handleGetBalance(UUID.randomUUID().toString());

    assertFalse(response.success());
    assertNull(response.data());
  }

  @Test
  void deposit_positiveAmount_shouldIncreaseBalance() {
    // Technique: EP
    UUID userId = UUID.randomUUID();
    userDao.putWallet(wallet(userId, "10"));

    Response response =
        service.handleDeposit(new DepositRequest(userId.toString(), BigDecimal.ONE));

    assertTrue(response.success());
    assertEquals(
        new BigDecimal("11"), userDao.findWalletByUserId(userId).orElseThrow().getBalance());
  }

  @Test
  void deposit_zeroAmount_shouldReturnFailure() {
    // Technique: BVA
    UUID userId = UUID.randomUUID();
    userDao.putWallet(wallet(userId, "10"));

    Response response =
        service.handleDeposit(new DepositRequest(userId.toString(), BigDecimal.ZERO));

    assertFalse(response.success());
    assertEquals(
        new BigDecimal("10"), userDao.findWalletByUserId(userId).orElseThrow().getBalance());
  }

  @Test
  void deposit_negativeAmount_shouldReturnFailure() {
    // Technique: BVA
    UUID userId = UUID.randomUUID();
    userDao.putWallet(wallet(userId, "10"));

    Response response =
        service.handleDeposit(new DepositRequest(userId.toString(), new BigDecimal("-1")));

    assertFalse(response.success());
    assertEquals(
        new BigDecimal("10"), userDao.findWalletByUserId(userId).orElseThrow().getBalance());
  }

  @Test
  void deposit_largeAmount_shouldNotOverflowBigDecimal() {
    // Technique: BVA
    UUID userId = UUID.randomUUID();
    userDao.putWallet(wallet(userId, "1"));
    BigDecimal amount = new BigDecimal("999999999999999999999999999999");

    Response response = service.handleDeposit(new DepositRequest(userId.toString(), amount));

    assertTrue(response.success());
    assertEquals(
        new BigDecimal("1000000000000000000000000000000"),
        userDao.findWalletByUserId(userId).orElseThrow().getBalance());
  }

  private void setPrivateField(String fieldName, Object value) throws Exception {
    Field field = service.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(service, value);
  }

  private User user(String username, String password, Role role, boolean active) {
    String salt = PasswordUtils.generateSalt();
    User user = role == Role.SELLER ? new Seller() : new Bidder();
    user.setId(UUID.randomUUID());
    user.setUsername(username);
    user.setPasswordSalt(salt);
    user.setPasswordHash(PasswordUtils.hashPassword(password, salt));
    user.setFullName(username);
    user.setEmail(username + "@example.com");
    user.setRole(role);
    user.setActive(active);
    user.setCreatedAt(LocalDateTime.now());
    user.setUpdatedAt(LocalDateTime.now());
    return user;
  }

  private Wallet wallet(UUID userId, String balance) {
    return new Wallet(
        UUID.randomUUID(),
        LocalDateTime.now(),
        LocalDateTime.now(),
        userId,
        new BigDecimal(balance));
  }
}
