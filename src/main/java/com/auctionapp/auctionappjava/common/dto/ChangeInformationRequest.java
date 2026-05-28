package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record ChangeInformationRequest(String userId, String fullName, String email)
    implements Serializable {}
