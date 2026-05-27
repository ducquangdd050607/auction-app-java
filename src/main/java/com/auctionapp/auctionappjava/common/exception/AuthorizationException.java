package com.auctionapp.auctionappjava.common.exception;

public class AuthorizationException extends AppException {
  public AuthorizationException(String message) {
    super(message);
  }
}
// mấy cái super message chỉ là truyền lại 1 lời "abc"
/*if (!password.equals(user.getPassword())) {
    throw new AuthException("Sai mật khẩu");
}*/
