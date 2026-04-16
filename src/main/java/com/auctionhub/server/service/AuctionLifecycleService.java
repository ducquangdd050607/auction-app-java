package com.auctionhub.server.service;

import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.model.Auction;

import java.time.LocalDateTime;

public class AuctionLifecycleService {
    public boolean refreshStatus(Auction auction, LocalDateTime now) {
        AuctionStatus previous = auction.getStatus();

        if (previous == AuctionStatus.CANCELED || previous == AuctionStatus.PAID) {
            return false;
        }

        if (now.isBefore(auction.getStartTime())) {
            auction.setStatus(AuctionStatus.OPEN);
        } else if (now.isBefore(auction.getEndTime())) {
            auction.setStatus(AuctionStatus.RUNNING);
        } else {
            auction.setStatus(AuctionStatus.FINISHED);
            if (auction.getWinnerId() == null) {
                auction.setWinnerId(auction.getLeadingBidderId());
            }
        }

        return previous != auction.getStatus();
    }

    public String explain(Auction auction) {
        return switch (auction.getStatus()) {
            case OPEN -> "Phiên chưa bắt đầu. Hệ thống sẽ tự chuyển sang RUNNING khi đến giờ mở phiên.";
            case RUNNING -> "Phiên đang diễn ra, bidder có thể đặt giá hợp lệ để cạnh tranh vị trí dẫn đầu.";
            case FINISHED -> "Phiên đã kết thúc, hệ thống đã khóa nhận bid mới và xác định người thắng.";
            case PAID -> "Phiên đã hoàn tất thanh toán. Đây là trạng thái cuối cùng sau FINISHED.";
            case CANCELED -> "Phiên đã bị hủy và không còn nhận hoặc xử lý bid.";
        };
    }
}
