package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.dto.AuctionRealtimeEvent;
import com.auctionapp.auctionappjava.server.realtime.AuctionRealtimeHub;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAuctionDao;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AuctionStatusService {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(50);
    private static AuctionDao auctionDao = new JdbcAuctionDao();

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
                scheduler.schedule(() -> executeOpenAuction(auction.getId()), delayToOpen, TimeUnit.MILLISECONDS);
            }
        }

        // Cài bộ đếm ĐÓNG PHIÊN (Nếu đang chạy hoặc sắp chạy)
        if (auction.getStatus() == AuctionStatus.OPEN || auction.getStatus() == AuctionStatus.RUNNING) {
            long delayToClose = ChronoUnit.MILLIS.between(now, auction.getEndTime());

            if (delayToClose <= 0) {
                scheduler.execute(() -> executeCloseAuction(auction.getId()));
            } else {
                scheduler.schedule(() -> executeCloseAuction(auction.getId()), delayToClose, TimeUnit.MILLISECONDS);
            }
        }
    }

    private static void executeOpenAuction(UUID auctionId) {
        try {
            Auction auction = auctionDao.findById(auctionId).orElse(null);
            if (auction != null && auction.getStatus() == AuctionStatus.OPEN) {

                // 1. Tính toán thời gian còn thiếu
                long remainingMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), auction.getStartTime());

                // 2. VÒNG LẶP CHỐNG THỨC SỚM
                while (remainingMillis > 0) {
                    Thread.sleep(remainingMillis);

                    // NẾU BỊ ĐÁNH THỨC, TÍNH LẠI XEM CÒN THIẾU BAO NHIÊU ĐỂ NGỦ TIẾP
                    remainingMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), auction.getStartTime());
                }

                // 3. TIẾN HÀNH MỞ PHIÊN (Lúc này CHẮC CHẮN 1000% đã qua giờ)
                auction.setStatus(AuctionStatus.RUNNING);
                auction.touch();
                auctionDao.save(auction);

                AuctionRealtimeHub.getInstance().broadcast(auction.getId(), new AuctionRealtimeEvent(
                        AuctionRealtimeEvent.AUCTION_STATUS_CHANGED,
                        null,
                        auction.getId(),
                        null,
                        null,
                        auction.getCurrentPrice(),
                        null,
                        auction.getLeadingBidderId(),
                        LocalDateTime.now(),
                        auction.getEndTime(),
                        false,
                        "Phiên đấu giá đã bắt đầu."
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executeCloseAuction(UUID auctionId) {
        try {
            while (true) {
                Auction auction = auctionDao.findById(auctionId).orElse(null);
                if (auction == null || auction.getStatus() != AuctionStatus.RUNNING) {
                    return;
                }

                long remainingMillis = ChronoUnit.MILLIS.between(LocalDateTime.now(), auction.getEndTime());
                if (remainingMillis > 0) {
                    Thread.sleep(remainingMillis);
                    continue;
                }

                auction.setStatus(AuctionStatus.FINISHED);
                auction.setWinnerId(auction.getLeadingBidderId());
                auction.touch();
                auctionDao.save(auction);

                AuctionRealtimeHub.getInstance().broadcast(auction.getId(), new AuctionRealtimeEvent(
                        AuctionRealtimeEvent.AUCTION_FINISHED,
                        null,
                        auction.getId(),
                        null,
                        null,
                        auction.getCurrentPrice(),
                        null,
                        auction.getLeadingBidderId(),
                        LocalDateTime.now(),
                        auction.getEndTime(),
                        false,
                        "Phiên đấu giá đã kết thúc."
                ));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recover bộ đếm giờ khi khởi động lại server sau tắt
    public static void recoverAndScheduleAll() {
        System.out.println("[MONITOR] Aloalo - Đã vào trong hàm!");
        try {
            System.out.println("[MONITOR] 1. Đang khởi tạo JdbcAuctionDao...");
            AuctionDao auctionDao = new JdbcAuctionDao();

            System.out.println("[MONITOR] 2. Khởi tạo DAO xong! Đang gọi lệnh findAll() xuống DB...");
            List<Auction> allAuctions = auctionDao.findAll();

            System.out.println("[MONITOR] 3. Đã lấy được dữ liệu từ DB! Đang lọc danh sách...");
            List<Auction> activeAuctions = allAuctions.stream()
                    .filter(a -> a.getStatus() != AuctionStatus.FINISHED)
                    .toList();

            System.out.println("[MONITOR] 4. Thành công! Đã tìm thấy " + activeAuctions.size() + " phiên cần hẹn giờ.");

            for (Auction auction : activeAuctions) {
                scheduleAuctionEvents(auction);
            }
        } catch (Exception e) {
            System.out.println("[MONITOR] PHÁT HIỆN LỖI RỒI NÀY:");
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        scheduler.shutdown();
    }
}