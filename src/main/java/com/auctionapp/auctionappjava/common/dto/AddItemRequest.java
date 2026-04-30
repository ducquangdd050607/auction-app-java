package com.auctionapp.auctionappjava.common.dto;
import java.io.Serializable;
import java.math.BigDecimal;

public record AddItemRequest(
        String sellerId,       // ID của người bán đang đăng nhập
        String title,          // Tên sản phẩm
        String description,    // Mô tả chi tiết
        BigDecimal startPrice, // Giá khởi điểm
        /*BigDecimal minIncrement,*/
        String itemType,       // Loại sản phẩm (ART, VEHICLE, ELECTRONIC...)
        int durationDays       // Số ngày đấu giá
) implements Serializable {}