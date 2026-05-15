package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record Response(boolean success, String message, Object data) implements Serializable {}
