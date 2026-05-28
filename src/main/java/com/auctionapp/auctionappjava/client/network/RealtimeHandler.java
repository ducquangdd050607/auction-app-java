package com.auctionapp.auctionappjava.client.network;

import com.auctionapp.auctionappjava.client.controllers.*;
import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.dto.LoginResponse;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import java.math.BigDecimal;
import java.util.UUID;

public class RealtimeHandler {

  public static void handlePushEvent(Response response) {
    String action = response.message();

    switch (action) {
      // Hoàn tiền bidder cũ và cộng tiền seller khi hết giờ
      case "SERVER_PUSH_BALANCE":
        BigDecimal newBalance = (BigDecimal) response.data();

        LoginResponse oldUser = UserSession.getInstance().getCurrentUser();
        if (oldUser != null) {
          UserSession.getInstance()
              .setCurrentUser(
                  new LoginResponse(
                      oldUser.id(),
                      oldUser.username(),
                      oldUser.fullName(),
                      oldUser.role(),
                      oldUser.email(),
                      newBalance,
                      oldUser.accStatus()));
        }
        // Ép màn hình tài khoản nhảy số luôn nếu đang mở
        if (AccountController.instance != null) {
          AccountController.instance.updateBalanceRealtime(newBalance);
        }
        break;

      // 2. CẬP NHẬT KHI CÓ SẢN PHẨM MỚI ĐƯỢC ĐĂNG BÁN
      case "SERVER_PUSH_NEW_AUCTION":
        if (AuctionListController.instance != null) {
          AuctionListController.instance.refreshListRealtime();
        }
        break;

      // 3. CẬP NHẬT KHI CÓ AI ĐÓ ĐẶT GIÁ (BID) MỚI THÀNH CÔNG
      case "SERVER_PUSH_NEW_BID":
        // Nhận mảng Object[] từ server
        Object[] pushData = (Object[]) response.data();
        UUID updatedAuctionId = (UUID) pushData[0];
        BigDecimal newPrice = (BigDecimal) pushData[1];
        UUID newBidderId = (UUID) pushData[2];
        String newEndTime = (String) pushData[3];

        if (AuctionListController.instance != null) {
          // Đổi tên hàm thành updateSingleRowPriceAndEndTime
          AuctionListController.instance.updateSingleRowPriceAndEndTime(
              updatedAuctionId, newPrice, newEndTime);
        }

        AuctionSummaryResponse currentAuction = AuctionSession.getInstance().getCurrentAuction();
        if (currentAuction != null
            && currentAuction != null
            && currentAuction.auctionId().equals(updatedAuctionId.toString())) {
          if (AuctionDetailController.instance != null) {
            // Đổi tên hàm để truyền thêm endTime
            AuctionDetailController.instance.updatePriceAndEndTimeRealtime(newPrice, newEndTime);
          }

          if (RankingListController.instance != null) {
            // Gọi hàm làm mới bảng và biểu đồ
            RankingListController.instance.refreshRankingRealtime();
          }

          String myUserId = UserSession.getInstance().getCurrentUser().id();
          if (ConfirmBiddingController.instance != null
              && !newBidderId.toString().equals(myUserId)) {
            ConfirmBiddingController.instance.showOutbidWarning();
          }
        }

        if (HistoryListController.instance != null) {
          HistoryListController.instance.refreshHistoryRealtime();
        }
        break;

      // Gói tin thông báo có tài khoản mới đăng ký
      case "SERVER_PUSH_NEW_USER":
        if (UsersManagerController.instance != null) {
          UsersManagerController.instance.refreshUsersRealtime();
        }
        break;

      // Sửa lại case FINISHED:
      case "SERVER_PUSH_AUCTION_FINISHED":
        UUID finishedAuctionId = (UUID) response.data();
        if (AuctionListController.instance != null) {
          AuctionListController.instance.updateSingleRowStatus(
              finishedAuctionId, AuctionStatus.FINISHED);
        }

        AuctionSummaryResponse currentActiveAuction =
            AuctionSession.getInstance().getCurrentAuction();

        if (AuctionDetailController.instance != null
            && currentActiveAuction != null // Chặn NullPointerException
            && currentActiveAuction.auctionId().equals(finishedAuctionId.toString())) {

          AuctionDetailController.instance.updateStatusRealtime(AuctionStatus.FINISHED);
        }
        break;

      // Thêm case STARTED:
      case "SERVER_PUSH_AUCTION_STARTED":
        UUID startedAuctionId = (UUID) response.data();
        if (AuctionListController.instance != null) {
          AuctionListController.instance.updateSingleRowStatus(
              startedAuctionId, AuctionStatus.RUNNING);
        }
        AuctionSummaryResponse currentAuctionStarted =
            AuctionSession.getInstance().getCurrentAuction();
        if (currentAuctionStarted != null
            && currentAuctionStarted.auctionId().equals(startedAuctionId.toString())) {
          if (AuctionDetailController.instance != null) {
            AuctionDetailController.instance.updateStatusRealtime(AuctionStatus.RUNNING);
          }
        }
        break;

      // Gói tin thông báo bình thường (Chỉ đọc, không click được)
      case "SERVER_PUSH_NOTIFICATION":
        String normalMsg = (String) response.data();
        if (NavigatorController.instance != null) {
          NavigatorController.instance.addNotification(normalMsg, "NORMAL");
        }
        break;

      // Gói tin thông báo về tiền (Click để chuyển trang)
      case "SERVER_PUSH_WALLET_NOTIFICATION":
        String walletMsg = (String) response.data();
        if (NavigatorController.instance != null) {
          NavigatorController.instance.addNotification(walletMsg, "WALLET");
        }
        break;

      // Gói tin thông báo bidder bị vượt mặt (Click để chuyển trang)
      case "SERVER_PUSH_OUTBID_NOTIFICATION":
        String outbidMsg = (String) response.data();
        if (NavigatorController.instance != null) {
          NavigatorController.instance.addNotification(outbidMsg, "OUTBID");
        }
        break;

      // Gói tin thông báo seller có giá mới (Click để chuyển trang)
      case "SERVER_PUSH_SELLER_BID_NOTIFICATION":
        String sellerBidMsg = (String) response.data();
        if (NavigatorController.instance != null) {
          NavigatorController.instance.addNotification(sellerBidMsg, "SELLER_BID");
        }
        break;

      // Gói tin thông báo đặt giá thành công
      case "SERVER_PUSH_BID_SUCCESS_NOTIFICATION":
        String bidSuccessMsg = (String) response.data();
        if (NavigatorController.instance != null) {
          NavigatorController.instance.addNotification(bidSuccessMsg, "BID_SUCCESS");
        }
        break;

      // Gói tin thông báo dành cho Admin
      case "SERVER_PUSH_ADMIN_NOTIFICATION":
        String adminMsg = (String) response.data();
        if (NavigatorController.instance != null) {
          NavigatorController.instance.addNotification(adminMsg, "ADMIN_SYS");
        }
        break;
    }
  }
}
