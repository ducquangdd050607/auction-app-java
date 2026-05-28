package com.auctionapp.auctionappjava.common.exception;

// lỗi request bị thiếu, null, sai định dạng trước khi vào nghiệp vụ chính.
public class InvalidRequestException extends AppException {
  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
