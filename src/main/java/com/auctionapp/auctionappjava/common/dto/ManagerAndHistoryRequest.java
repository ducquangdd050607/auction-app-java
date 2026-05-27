package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;

public record ManagerAndHistoryRequest(String userId) implements Serializable {}

// - 2-in-1, huh
// - Make it three. For Ban func.
// - Nah just remove the Bid one
