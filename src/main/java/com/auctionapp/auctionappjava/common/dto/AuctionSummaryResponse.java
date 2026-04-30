package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record AuctionSummaryResponse(
        String auctionId,
        String category,
        String itemName,
        /*String imagePath,*/ // Đường dẫn ảnh thu nhỏ (kbt có cần hay không nên cmt lại)
        BigDecimal currentPrice,   // Giá hiện tại
        BigDecimal startPrice,     // Khởi đầu
        /*BigDecimal stepPrice, */     // Bước giá
        /*long*/String timeLeft, // Thời gian còn lại (tạm để String test Socket trước)
        String status,
        int bidderCount  // Số bidder quan tâm
) implements Serializable {}