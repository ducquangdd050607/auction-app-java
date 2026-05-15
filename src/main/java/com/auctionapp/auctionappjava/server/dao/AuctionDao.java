package com.auctionapp.auctionappjava.server.dao;

<<<<<<< HEAD
=======
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
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

<<<<<<< HEAD
=======
    List<AuctionSummaryResponse> findAllSummaries();

    List<AuctionSummaryResponse> findSummariesBySellerId(UUID sellerId);


>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
    Optional<Auction> findLatestAuctionCreatedBySellerId(UUID sellerId);

    Optional<Auction> findMostBiddedAuction();

    long countAuctionsCreatedBySellerId(UUID sellerId);

    void deleteById(UUID auctionId);

}
