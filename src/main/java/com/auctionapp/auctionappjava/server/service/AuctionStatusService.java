package com.auctionapp.auctionappjava.server.service;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;

import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.dao.NotificationDao;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAuctionDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcNotificationDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcUserDao;
import com.auctionapp.auctionappjava.server.model.Auction;
import com.auctionapp.auctionappjava.server.model.User;
import com.auctionapp.auctionappjava.server.model.Wallet;
import com.auctionapp.auctionappjava.server.network.SessionManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionStatusService {
  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(50);
  private static AuctionDao auctionDao = new JdbcAuctionDao();
  private static final UserDao userDao = new JdbcUserDao();
  private static final NotificationDao notificationDao = new JdbcNotificationDao();

  // Cho phép inject dao trong test
  public static void setAuctionDao(AuctionDao dao) {
    if (dao != null) auctionDao = dao;
  }

  // Đặt bộ đếm giờ cho 1 phiên đấu giá
  public static void scheduleAuctionEvents(Auction auction) {
    LocalDateTime now = LocalDateTime.now();

    // Cài bộ đếm MỞ PHIÊN (Nếu đang chờ mở)
    if (auction.getStatus() == AuctionStatus.OPEN) {
      long delayToOpen = ChronoUnit.MILLIS.between(now, auction.getStartTime());

      if (delayToOpen <= 0) {
        // Đã quá giờ -> Mở luôn lập tức
        scheduler.execute(() -> executeOpenAuction(auction.getId()));
      } else {
        // Hẹn giờ mở bằng ScheduledExecutorService
        scheduler.schedule(
            () -> executeOpenAuction(auction.getId()), delayToOpen, TimeUnit.MILLISECONDS);
      }
    }

    // Cài bộ đếm ĐÓNG PHIÊN (Nếu đang chạy hoặc sắp chạy)
    if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING) {
      long delayToClose = ChronoUnit.MILLIS.between(now, auction.getEndTime());

      if (delayToClose <= 0) {
        scheduler.execute(() -> executeCloseAuction(auction.getId()));
      } else {
        scheduler.schedule(
            () -> executeCloseAuction(auction.getId()), delayToClose, TimeUnit.MILLISECONDS);
      }
    }
  }

  private static void executeOpenAuction(UUID auctionId) {
    try {
      Auction auction = auctionDao.findById(auctionId).orElse(null);
      if (auction != null && auction.getStatus() == AuctionStatus.OPEN) {

        // 1. Tính toán thời gian còn thiếu
        long remainingMillis =
            ChronoUnit.MILLIS.between(LocalDateTime.now(), auction.getStartTime());

        // 2. VÒNG LẶP CHỐNG THỨC SỚM
        while (remainingMillis > 0) {
          Thread.sleep(remainingMillis);

          // NẾU BỊ ĐÁNH THỨC, TÍNH LẠI XEM CÒN THIẾU BAO NHIÊU ĐỂ NGỦ TIẾP
          remainingMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), auction.getStartTime());
        }

        // 3. TIẾN HÀNH MỞ PHIÊN (Lúc này CHẮC CHẮN 1000% đã qua giờ)
        auction.setStatus(AuctionStatus.RUNNING);
        auctionDao.save(auction);

        SessionManager.getInstance()
            .broadcast(new Response(true, "SERVER_PUSH_AUCTION_STARTED", auction.getId()));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static void executeCloseAuction(UUID auctionId) {
    try {
      Auction auction = auctionDao.findById(auctionId).orElse(null);
      if (auction != null && auction.getStatus() == AuctionStatus.RUNNING) {

        // 1. Tính toán thời gian còn thiếu
        long remainingMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), auction.getEndTime());

        // 2. VÒNG LẶP CHỐNG THỨC SỚM
        while (remainingMillis > 0) {
          Thread.sleep(remainingMillis);

          // NẾU BỊ ĐÁNH THỨC, TÍNH LẠI XEM CÒN THIẾU BAO NHIÊU ĐỂ NGỦ TIẾP
          remainingMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), auction.getEndTime());
        }

        // 3. TIẾN HÀNH ĐÓNG PHIÊN (Lúc này CHẮC CHẮN 1000% đã qua giờ)
        auction.setStatus(AuctionStatus.FINISHED);
        // Nếu có người đặt giá cao nhất (leading_bidder_id != null), nghĩa là phiên thành công
        if (auction.getLeadingBidderId() != null) {
          // Chốt người thắng
          auction.setWinnerId(auction.getLeadingBidderId());
          String winnerName =
              userDao
                  .findById(auction.getLeadingBidderId())
                  .map(User::getFullName)
                  .orElse(auction.getLeadingBidderId().toString());

          // Lấy số tiền thắng cuộc (giá hiện tại)
          BigDecimal winningAmount = auction.getCurrentPrice();
          UUID sellerId = auction.getSellerId();

          // Lấy ví của Seller và cộng tiền
          Optional<Wallet> sellerWalletOpt = userDao.findWalletByUserId(sellerId);

          if (sellerWalletOpt.isPresent()) {
            Wallet sellerWallet = sellerWalletOpt.get();

            // Cộng tiền thắng vào số dư hiện tại của Seller
            sellerWallet.setBalance(sellerWallet.getBalance().add(winningAmount));

            // Lưu lại ví
            userDao.saveWallet(sellerWallet);

            // Cộng tiền cho seller
            Response sellerPaidMsg =
                new Response(true, "SERVER_PUSH_BALANCE", sellerWallet.getBalance());
            SessionManager.getInstance().sendToUser(sellerId.toString(), sellerPaidMsg);

            // 1. Gửi thông báo cho Seller (Người bán)
            String sellerNotiMsg =
                "💰 Phiên đấu giá của bạn đã kết thúc thành công! Số tiền "
                    + formatMoney(winningAmount)
                    + " VNĐ đã được cộng vào ví.";
            notificationDao.createNotification(sellerId, auctionId, "WALLET", sellerNotiMsg);
            SessionManager.getInstance()
                .sendToUser(
                    sellerId.toString(),
                    new Response(true, "SERVER_PUSH_WALLET_NOTIFICATION", sellerNotiMsg));

            // 2. Gửi thông báo cho Winner (Người thắng)
            String winnerNotiMsg =
                "🎉 Chúc mừng! Bạn đã thắng phiên đấu giá. Hệ thống đã khấu trừ "
                    + formatMoney(winningAmount)
                    + " VNĐ từ ví của bạn.";
            notificationDao.createNotification(
                auction.getLeadingBidderId(), auctionId, "WALLET", winnerNotiMsg);
            SessionManager.getInstance()
                .sendToUser(
                    auction.getLeadingBidderId().toString(),
                    new Response(true, "SERVER_PUSH_WALLET_NOTIFICATION", winnerNotiMsg));
          }
        } else {
          // Xử lý khi phiên đấu giá kết thúc nhưng không có ai đặt giá
          UUID sellerId = auction.getSellerId();
          String sellerNotiMsg =
              "❌ Phiên đấu giá của bạn đã kết thúc nhưng không có lượt đặt giá nào. Sản phẩm chưa được bán và không có tiền cộng vào ví.";
          SessionManager.getInstance()
              .sendToUser(
                  sellerId.toString(),
                  new Response(true, "SERVER_PUSH_WALLET_NOTIFICATION", sellerNotiMsg));
        }

        auctionDao.save(auction);

        SessionManager.getInstance()
            .broadcast(new Response(true, "SERVER_PUSH_AUCTION_FINISHED", auction.getId()));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // Recover bộ đếm giờ khi khởi động lại server sau tắt
  public static void recoverAndScheduleAll() {
    try {
      AuctionDao auctionDao = new JdbcAuctionDao();

      System.out.println("Dang tim cac phien dau gia can hen gio...");
      List<Auction> allAuctions = auctionDao.findAll();

      List<Auction> activeAuctions =
          allAuctions.stream().filter(a -> a.getStatus() != AuctionStatus.FINISHED).toList();

      System.out.println(
          "Thanh cong! Da tim thay " + activeAuctions.size() + " phien can hen gio.");

      for (Auction auction : activeAuctions) {
        scheduleAuctionEvents(auction);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void shutdown() {
    scheduler.shutdown();
  }
}
