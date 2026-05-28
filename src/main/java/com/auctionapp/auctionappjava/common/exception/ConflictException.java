package com.auctionapp.auctionappjava.common.exception;

// lỗi xảy ra khi dữ liệu bị xung đột / không hợp lệ với trạng thái hiện tại
public class ConflictException extends AppException {
  public ConflictException(String message) {
    super(message);
  }
}
