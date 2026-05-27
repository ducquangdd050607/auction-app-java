package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record ImageResponse(byte[] imageData) implements Serializable {}
