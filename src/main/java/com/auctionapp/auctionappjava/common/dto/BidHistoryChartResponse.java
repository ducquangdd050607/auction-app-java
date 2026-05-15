package com.auctionapp.auctionappjava.common.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record BidHistoryChartResponse(
        UUID auctionId,
        List<BidHistoryPointDto> points
) implements Serializable {}
