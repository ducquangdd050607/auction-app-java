package com.auctionapp.auctionappjava.common.enums;

public enum RequestAction {
  LOGIN, // đăng nhập
  REGISTER, // dki
  LOGOUT, // đăng xuất
  LIST_AUCTIONS, // xem danh sách đấu giá
  GET_AUCTION_DETAIL, // xem chi tiết 1 auction
  SUBSCRIBE_AUCTION, //  Client đăng ký nhận EVENT realtime
  UNSUBSCRIBE_AUCTION,
  PLACE_BID, // đặt giá
  CONFIGURE_AUTO_BID, // autobid
  CREATE_AUCTION, // tạo
  UPDATE_AUCTION, // thay đổi
  DELETE_AUCTION, // xóa
  CANCEL_AUCTION, // hủy
  MARK_AUCTION_PAID, // xác nhận thanh toán
  LIST_MY_AUCTIONS, // danh sách auction của mình
  LIST_USERS, // Admin panel
  ADMIN_OVERVIEW // Admin panel
}
