package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record Request(String action, Object payload) implements Serializable {}
