package com.auctionapp.auctionappjava.common.exception;
// RuntimeException Là lỗi không bắt buộc phải try-catch, extend RuntimeException là unchecked exception
public class AppException extends RuntimeException {
    public AppException(String message)
    {
        //Chỉ muốn báo lỗi bằng message
        super(message);
        // VD throw new AppException("User không tồn tại");
    }

    public AppException(String message, Throwable cause)
    {
        super(message, cause);//muốn bọc (wrap) lỗi gốc
        // cause là lỗi gốc
    }
}
