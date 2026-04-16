package com.auctionhub.server.dao;

import com.auctionhub.common.enums.AuctionStatus;
import com.auctionhub.common.model.Auction;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AuctionDao {
    Auction save(Auction auction);

    Auction update(Auction auction);

    Optional<Auction> findById(UUID id);

    List<Auction> findAll();

    List<Auction> findBySellerId(UUID sellerId);

    List<Auction> findByStatuses(Collection<AuctionStatus> statuses);

    void deleteById(UUID id);

    long count();
}
