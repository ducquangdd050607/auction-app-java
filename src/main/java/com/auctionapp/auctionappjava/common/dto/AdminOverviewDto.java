package com.auctionapp.auctionappjava.common.dto;
/*DTO (Data Transfer Object) là các object chỉ dùng để truyền dữ liệu giữa các
layer như client ↔ server, controller ↔ service, hoặc giữa các module.
DTO thường không chứa business logic, chỉ chứa dữ liệu cần gửi đi hoặc nhận vào.*/
import java.io.Serializable;
import java.math.BigDecimal;

public record AdminOverviewDto(
        long totalUsers,
        long totalAuctions,
        long runningAuctions,
        long finishedAuctions,
        BigDecimal totalBidVolume
) implements Serializable {
}
//DTO này dùng cho màn hình tổng quan của admin