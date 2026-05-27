package com.auctionapp.auctionappjava.common.exception;

// lỗi khi dữ liệu nhập vào sai format / thiếu / không hợp lệ
public class ValidationException extends AppException {
  public ValidationException(String message) {
    super(message);
  }
}
