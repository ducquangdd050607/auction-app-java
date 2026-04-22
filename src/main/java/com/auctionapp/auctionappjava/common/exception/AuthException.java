package com.auctionapp.auctionappjava.common.exception;
//một loại lỗi cụ thể: lỗi liên quan đến đăng nhập / quyền
public class AuthException extends AppException {
    public AuthException(String message) {
        super(message);
    }
}
