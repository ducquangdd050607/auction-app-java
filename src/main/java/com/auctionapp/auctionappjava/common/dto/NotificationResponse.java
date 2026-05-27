package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

// Dùng record tự động có getter và implement sẵn Serializable để truyền qua Socket
public record NotificationResponse(String type, String message) implements Serializable {}
