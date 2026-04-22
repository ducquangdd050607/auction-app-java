package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryDto;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.Auction;

import java.util.*;

public interface AuctionDao {
    Auction save(Auction a);

    Optional<Auction> findById(UUID id);

    List<AuctionSummaryDto> findSummaries();

    List<AuctionSummaryDto> findSummariesBySeller(UUID sellerId);

    void update(Auction a);

    void updateStatus(UUID id, AuctionStatus s);

    void delete(UUID id);

    long countAll();

    long countByStatus(AuctionStatus s);
}
