package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionDao {
    void save(Auction auction);
    void update(Auction auction);
    void delete(UUID auctionId);
    Optional<Auction> findById(UUID auctionId);
    List<Auction> findAll();
    List<Auction> findBySellerId(UUID sellerId);
    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findEndingBefore(LocalDateTime now);
    List<Auction> findStartingBefore(LocalDateTime now);
    long countAll();
    long countByStatus(AuctionStatus status);
}
