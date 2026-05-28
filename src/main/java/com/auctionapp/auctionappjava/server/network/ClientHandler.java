package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.server.dao.NotificationDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcNotificationDao;
import com.auctionapp.auctionappjava.server.model.Notification;
import com.auctionapp.auctionappjava.server.service.AuctionService;
import com.auctionapp.auctionappjava.server.service.AuctionTrendService;
import com.auctionapp.auctionappjava.server.service.UserService;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClientHandler implements Runnable {
  private Socket socket;

  // Khởi tạo các Service
  private final UserService userService = new UserService();
  private final AuctionTrendService auctionTrendService = new AuctionTrendService();
  private final AuctionService auctionService = new AuctionService();

  public ClientHandler(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {
    String threadName = Thread.currentThread().getName();
    System.out.println("[" + threadName + "] Bat dau phuc vu Client: " + socket.getInetAddress());
    ObjectOutputStream out = null;
    ObjectInputStream in = null;
    String currentUserId = null;

    try {
      out = new ObjectOutputStream(socket.getOutputStream());
      out.flush();
      in = new ObjectInputStream(socket.getInputStream());

      while (true) {
        Request request = (Request) in.readObject();
        Response response = null;

        switch (request.action()) {
          // Case thuộc user
          case "LOGIN":
            response = userService.handleLogin((LoginRequest) request.payload());
            if (response.success() && response.data() != null) {
              LoginResponse loginRes = (LoginResponse) response.data();
              currentUserId = loginRes.id();
              SessionManager.getInstance().registerSession(currentUserId, out);
            }
            break;
          case "REGISTER":
            response = userService.handleRegister((RegisterRequest) request.payload());
            break;
          case "DEPOSIT":
            response = userService.handleDeposit((DepositRequest) request.payload());
            break;
          case "CHANGE_INFORMATION":
            response =
                userService.handleChangeInformation((ChangeInformationRequest) request.payload());
            break;
          case "CHANGE_PASSWORD":
            response = userService.handleChangePassword((ChangePasswordRequest) request.payload());
            break;
          case "GET_BALANCE":
            response = userService.handleGetBalance((String) request.payload());
            break;
          case "GET_STAT":
            response = userService.handleGetStats((String) request.payload());
            break;

          // Case thuộc auction
          case "GET_ALL_AUCTIONS":
            response = auctionService.handleGetAllAuctions();
            break;
          case "GET_ALL_UPLOADED_AUCTIONS":
            response =
                auctionService.handleGetAllUploadedAuctions(
                    (ManagerAndHistoryRequest) request.payload());
            break;
          case "GET_ALL_FEATURED_AUCTIONS":
            response = auctionService.handleGetAllFeaturedAuctions();
            break;
          case "GET_AUCTION_TRENDS":
            response = auctionTrendService.handleGetAuctionTrends();
            break;
          case "GET_HISTORY":
            response =
                auctionService.handleGetAllPersonalBiddedAuctions(
                    (ManagerAndHistoryRequest) request.payload());
            break;
          case "GET_TRANSACTIONS":
            response =
                auctionService.handleGetAllBiddedAuctions(
                    (ManagerAndHistoryRequest) request.payload());
            break;
          case "GET_BID_RANKING":
            response =
                auctionService.handleGetBidRanking((ManagerAndHistoryRequest) request.payload());
            break;
          case "ADD_ITEM":
            response = auctionService.handleAddItem((AddItemRequest) request.payload());
            break;
          case "PLACE_BID":
            response = auctionService.handlePlaceBid((PlaceBidRequest) request.payload());
            break;
          // THEM AUTO-BID REQUEST: nhan cau hinh auto-bid tu client va luu xuong DB.
          case "CONFIGURE_AUTO_BID":
            response =
                auctionService.handleConfigureAutoBid((ConfigureAutoBidRequest) request.payload());
            break;
          case "GET_USERS":
            response = auctionService.handleGetUsers();
            break;
          case "REMOVE_AUCTION":
            response = auctionService.handleRemoveAuction((RemoveAuctionRequest) request.payload());
            break;
          case "DECIDE_STATUS":
            response =
                auctionService.handleSetUserStatus((ManagerAndHistoryRequest) request.payload());
            break;
          case "GET_IMAGE":
            response = auctionService.handleGetImage((ImageRequest) request.payload());
            break;

          // Thêm request lấy noti
          case "GET_NOTIFICATIONS":
            String userId = (String) request.payload();
            try {
              NotificationDao notiDao = new JdbcNotificationDao();
              // Lấy list từ DB ra
              List<Notification> notiListDb = notiDao.findByUserId(UUID.fromString(userId));
              List<NotificationResponse> notiResponseList = new ArrayList<>();
              for (Notification n : notiListDb) {
                notiResponseList.add(new NotificationResponse(n.getType(), n.getMessage()));
              }

              // Gửi danh sách DTO đi
              response = new Response(true, "Lấy thông báo thành công", notiResponseList);
            } catch (Exception e) {
              response = new Response(false, "Loi lay thong bao", null);
            }
            break;

          default:
            response = new Response(false, "Hành động không hợp lệ: " + request.action(), null);
            break;
        }

        // Gửi kết quả duy nhất 1 lần ở đây
        if (response != null) {
          out.writeObject(response);
          out.flush();
        }
      }

    } catch (Exception e) {
      System.out.println("[" + threadName + "] Client da ngat ket noi. Loi: " + e.getMessage());
    } finally {
      if (currentUserId != null) {
        SessionManager.getInstance().removeSession(currentUserId);
      }

      try {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null) socket.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
