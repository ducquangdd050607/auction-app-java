package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public record UserDetailResponse(
    String userId,
    String latestBid,
    String fullName,
    String role,
    BigDecimal balance,
    boolean accStatus,
    int bids)
    implements Serializable {
  // UserManager
  // Đề xuất tách Detail của Admin riêng
}
