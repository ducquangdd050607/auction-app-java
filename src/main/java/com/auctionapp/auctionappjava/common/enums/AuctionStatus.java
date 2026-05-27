package com.auctionapp.auctionappjava.common.enums;

public enum AuctionStatus {
  OPEN,
  RUNNING,
  FINISHED,
  PAID,
  CANCELED; // dấu ; để kết thúc

  public boolean isClosedForBidding() {
    return this == FINISHED || this == PAID || this == CANCELED;
  }
  // method kiểm tra xem đã đóng đấu giá hay chưa, this đại diện cho enum hiện tại
  // ý nghĩa nếu trạng thái là 1 trong 3 cái kia là đã đóng đấu giá
}
// AuctionStatus a = AuctionStatus.RUNNING;
