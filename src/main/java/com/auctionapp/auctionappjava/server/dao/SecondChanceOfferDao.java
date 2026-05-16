package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.enums.SecondChanceOfferStatus;
import com.auctionapp.auctionappjava.common.model.SecondChanceOffer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecondChanceOfferDao {
    SecondChanceOffer save(SecondChanceOffer offer);

    Optional<SecondChanceOffer> findById(UUID id);

    List<SecondChanceOffer> findByAuctionId(UUID auctionId);

    List<SecondChanceOffer> findByStatus(SecondChanceOfferStatus status);

    List<SecondChanceOffer> findExpiredOpenOffers(LocalDateTime now);
}
