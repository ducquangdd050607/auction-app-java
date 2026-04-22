package com.auctionapp.auctionappjava.common.enums;
//Phân loại message để hệ thống biết phải xử lý kiểu gì
//Phân loại loại message gửi qua network
public enum MessageKind {
    REQUEST, //Client yêu cầu server làm gì đó
    RESPONSE, //Message từ server → client (trả lời request) phản hồi
    EVENT //Message từ server → client (tự push, không cần request)
    //Server chủ động gửi update realtime

}
