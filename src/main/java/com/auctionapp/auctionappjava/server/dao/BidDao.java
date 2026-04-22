package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.dto.BidDto;
import com.auctionapp.auctionappjava.common.model.BidTransaction;

import java.math.BigDecimal;
import java.util.*;

public interface BidDao {
    BidTransaction save(BidTransaction b);

    List<BidDto> findByAuction(UUID auctionId);

    long countByBidder(UUID bidderId);

    BigDecimal sumAmount();
}
