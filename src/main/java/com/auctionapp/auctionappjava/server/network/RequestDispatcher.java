package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.server.dao.NotificationDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcNotificationDao;
import com.auctionapp.auctionappjava.server.model.Notification;
import com.auctionapp.auctionappjava.server.service.AuctionService;
import com.auctionapp.auctionappjava.server.service.AuctionTrendService;
import com.auctionapp.auctionappjava.server.service.UserService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RequestDispatcher {
  private final UserService userService;
  private final AuctionTrendService auctionTrendService;
  private final AuctionService auctionService;
  private final NotificationDao notificationDao;

  public RequestDispatcher() {
    this(
        new UserService(),
        new AuctionTrendService(),
        new AuctionService(),
        new JdbcNotificationDao());
  }

  RequestDispatcher(
      UserService userService,
      AuctionTrendService auctionTrendService,
      AuctionService auctionService,
      NotificationDao notificationDao) {
    this.userService = userService;
    this.auctionTrendService = auctionTrendService;
    this.auctionService = auctionService;
    this.notificationDao = notificationDao;
  }

  public Response dispatch(Request request) {
    if (request == null || request.action() == null) {
      return new Response(false, "Hanh dong khong hop le", null);
    }

    return switch (request.action()) {
      case "LOGIN" -> userService.handleLogin((LoginRequest) request.payload());
      case "REGISTER" -> userService.handleRegister((RegisterRequest) request.payload());
      case "DEPOSIT" -> userService.handleDeposit((DepositRequest) request.payload());
      case "CHANGE_INFORMATION" ->
          userService.handleChangeInformation((ChangeInformationRequest) request.payload());
      case "CHANGE_PASSWORD" ->
          userService.handleChangePassword((ChangePasswordRequest) request.payload());
      case "GET_BALANCE" -> userService.handleGetBalance((String) request.payload());
      case "GET_STAT" -> userService.handleGetStats((String) request.payload());
      case "GET_ALL_AUCTIONS" -> auctionService.handleGetAllAuctions();
      case "GET_ALL_UPLOADED_AUCTIONS" ->
          auctionService.handleGetAllUploadedAuctions((ManagerAndHistoryRequest) request.payload());
      case "GET_ALL_FEATURED_AUCTIONS" -> auctionService.handleGetAllFeaturedAuctions();
      case "GET_AUCTION_TRENDS" -> auctionTrendService.handleGetAuctionTrends();
      case "GET_HISTORY" ->
          auctionService.handleGetAllPersonalBiddedAuctions(
              (ManagerAndHistoryRequest) request.payload());
      case "GET_TRANSACTIONS" ->
          auctionService.handleGetAllBiddedAuctions((ManagerAndHistoryRequest) request.payload());
      case "GET_BID_RANKING" ->
          auctionService.handleGetBidRanking((ManagerAndHistoryRequest) request.payload());
      case "ADD_ITEM" -> auctionService.handleAddItem((AddItemRequest) request.payload());
      case "PLACE_BID" -> auctionService.handlePlaceBid((PlaceBidRequest) request.payload());
      case "CONFIGURE_AUTO_BID" ->
          auctionService.handleConfigureAutoBid((ConfigureAutoBidRequest) request.payload());
      case "GET_AUTO_BID" -> auctionService.handleGetAutoBid((AutoBidRequest) request.payload());
      case "DISABLE_AUTO_BID" ->
          auctionService.handleDisableAutoBid((AutoBidRequest) request.payload());
      case "GET_USERS" -> auctionService.handleGetUsers();
      case "REMOVE_AUCTION" ->
          auctionService.handleRemoveAuction((RemoveAuctionRequest) request.payload());
      case "DECIDE_STATUS" ->
          auctionService.handleSetUserStatus((ManagerAndHistoryRequest) request.payload());
      case "GET_IMAGE" -> auctionService.handleGetImage((ImageRequest) request.payload());
      case "GET_NOTIFICATIONS" -> getNotifications((String) request.payload());
      case "LOGOUT" -> new Response(true, "Đã đăng xuất khỏi Server", null);
      default -> new Response(false, "Hành động không hợp lệ: " + request.action(), null);
    };
  }

  private Response getNotifications(String userId) {
    try {
      List<Notification> notiListDb = notificationDao.findByUserId(UUID.fromString(userId));
      List<NotificationResponse> notiResponseList = new ArrayList<>();
      for (Notification notification : notiListDb) {
        notiResponseList.add(
            new NotificationResponse(notification.getType(), notification.getMessage()));
      }
      return new Response(true, "Lay thong bao thanh cong", notiResponseList);
    } catch (Exception e) {
      return new Response(false, "Loi lay thong bao", null);
    }
  }
}
