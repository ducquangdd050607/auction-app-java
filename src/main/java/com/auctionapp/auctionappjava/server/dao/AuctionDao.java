package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionDao {
    Auction save(Auction auction);

    Optional<Auction> findById(UUID auctionId);

    List<Auction> findByStatus(AuctionStatus status);

    List<Auction> findAll();

    List<Auction> findBySellerId(UUID sellerId);

    List<AuctionSummaryResponse> findAllSummaries();

    List<AuctionSummaryResponse> findSummariesBySellerId(UUID sellerId);

    Optional<Auction> findLatestAuctionCreatedBySellerId(UUID sellerId);

    Optional<Auction> findMostExpiredAuction();

    Optional<Auction> findMostBiddedAuction();

    long countAuctionsCreatedBySellerId(UUID sellerId);

    void deleteById(UUID auctionId);

}
