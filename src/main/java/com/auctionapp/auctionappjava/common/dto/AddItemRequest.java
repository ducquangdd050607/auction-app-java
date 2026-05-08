package com.auctionapp.auctionappjava.common.dto;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AddItemRequest(
        String sellerId,       // ID của người bán đang đăng nhập
        String title,          // Tên sản phẩm
        String description,    // Mô tả chi tiết
        BigDecimal startPrice, // Giá khởi điểm
        BigDecimal minIncrement,
        String itemType,       // Loại sản phẩm (ART, VEHICLE, ELECTRONIC...)
        LocalDateTime openTime,
        LocalDateTime endTime,
        String attribute1,
        String attribute2
) implements Serializable {}