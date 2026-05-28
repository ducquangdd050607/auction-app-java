package com.auctionapp.auctionappjava.server.network;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.dto.Response;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SessionManagerTest {

  private final SessionManager sessionManager = SessionManager.getInstance();
  private String userId;

  @AfterEach
  void tearDown() {
    if (userId != null) {
      sessionManager.removeSession(userId);
    }
  }

  @Test
  void logout_loggedInUser_shouldClearCurrentSession() throws Exception {
    // Technique: EP
    userId = UUID.randomUUID().toString();
    sessionManager.registerSession(userId, new ObjectOutputStream(new ByteArrayOutputStream()));

    Response response = sessionManager.handleLogout(userId);

    assertTrue(response.success());
    assertNull(response.data());
    assertFalse(sessionManager.hasSession(userId));
  }

  @Test
  void logout_withoutLogin_shouldReturnSuccessWithoutCrash() {
    // Technique: EP
    userId = UUID.randomUUID().toString();

    Response response = assertDoesNotThrow(() -> sessionManager.handleLogout(userId));

    assertTrue(response.success());
    assertNull(response.data());
    assertFalse(sessionManager.hasSession(userId));
  }

  @Test
  void logout_nullSession_shouldReturnFailureOrSafeResponse() {
    // Technique: EP
    Response response = assertDoesNotThrow(() -> sessionManager.handleLogout(null));

    assertNotNull(response);
    assertFalse(response.success());
    assertNull(response.data());
  }
}
