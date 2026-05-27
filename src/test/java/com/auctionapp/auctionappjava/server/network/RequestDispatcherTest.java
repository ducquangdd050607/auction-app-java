package com.auctionapp.auctionappjava.server.network;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.server.dao.NotificationDao;
import com.auctionapp.auctionappjava.server.model.Notification;
import com.auctionapp.auctionappjava.server.service.AuctionService;
import com.auctionapp.auctionappjava.server.service.AuctionTrendService;
import com.auctionapp.auctionappjava.server.service.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestDispatcherTest {

  private TrackingUserService userService;
  private TrackingAuctionService auctionService;
  private TrackingNotificationDao notificationDao;
  private RequestDispatcher dispatcher;

  @BeforeEach
  void setUp() {
    userService = new TrackingUserService();
    auctionService = new TrackingAuctionService();
    notificationDao = new TrackingNotificationDao();
    dispatcher =
        new RequestDispatcher(
            userService, new AuctionTrendService(), auctionService, notificationDao);
  }

  @Test
  void dispatch_loginRequest_shouldCallUserServiceLogin() {
    // Technique: EP
    Response response =
        dispatcher.dispatch(new Request("LOGIN", new LoginRequest("bidder01", "Binh@123456")));

    assertTrue(response.success());
    assertTrue(userService.loginCalled);
  }

  @Test
  void dispatch_registerRequest_shouldCallUserServiceRegister() {
    // Technique: EP
    Response response =
        dispatcher.dispatch(
            new Request(
                "REGISTER",
                new RegisterRequest("u", "Password1", "User", "u@example.com", "BIDDER")));

    assertTrue(response.success());
    assertTrue(userService.registerCalled);
  }

  @Test
  void dispatch_placeBidRequest_shouldCallAuctionService() {
    // Technique: EP
    Response response =
        dispatcher.dispatch(
            new Request(
                "PLACE_BID",
                new PlaceBidRequest(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN)));

    assertTrue(response.success());
    assertTrue(auctionService.placeBidCalled);
  }

  @Test
  void dispatch_getImageRequest_shouldCallAuctionServiceGetImage() {
    // Technique: EP
    Response response =
        dispatcher.dispatch(
            new Request("GET_IMAGE", new ImageRequest(UUID.randomUUID().toString())));

    assertTrue(response.success());
    assertTrue(auctionService.getImageCalled);
  }

  @Test
  void dispatch_getNotificationsRequest_shouldCallNotificationDao() {
    // Technique: EP
    UUID userId = UUID.randomUUID();

    Response response = dispatcher.dispatch(new Request("GET_NOTIFICATIONS", userId.toString()));

    assertTrue(response.success());
    assertTrue(notificationDao.findCalled);
    assertEquals(1, ((List<?>) response.data()).size());
  }

  @Test
  void dispatch_unknownRequest_shouldReturnFailureWithoutCrash() {
    // Technique: EP
    Response response = dispatcher.dispatch(new Request("UNKNOWN", null));

    assertFalse(response.success());
  }

  private static class TrackingUserService extends UserService {
    boolean loginCalled;
    boolean registerCalled;

    @Override
    public Response handleLogin(LoginRequest loginData) {
      loginCalled = true;
      return new Response(true, "login", null);
    }

    @Override
    public Response handleRegister(RegisterRequest registerData) {
      registerCalled = true;
      return new Response(true, "register", null);
    }
  }

  private static class TrackingAuctionService extends AuctionService {
    boolean placeBidCalled;
    boolean getImageCalled;

    @Override
    public Response handlePlaceBid(PlaceBidRequest placeBidData) {
      placeBidCalled = true;
      return new Response(true, "place bid", null);
    }

    @Override
    public Response handleGetImage(ImageRequest request) {
      getImageCalled = true;
      return new Response(true, "image", null);
    }
  }

  private static class TrackingNotificationDao implements NotificationDao {
    boolean findCalled;

    @Override
    public void createNotification(UUID userId, UUID auctionId, String type, String message) {}

    @Override
    public List<Notification> findByUserId(UUID userId) {
      findCalled = true;
      return List.of(
          new Notification(
              UUID.randomUUID(), userId, null, "INFO", "message", LocalDateTime.now()));
    }

    @Override
    public void deleteByUserId(UUID userId) {}
  }
}
