package com.auctionapp.auctionappjava.server.service.postauction;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.PaymentDeadlineStatus;
import com.auctionapp.auctionappjava.common.enums.SecondChanceOfferStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.model.BidTransaction;
import com.auctionapp.auctionappjava.common.model.PaymentDeadline;
import com.auctionapp.auctionappjava.common.model.SecondChanceOffer;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.dao.BidDao;
import com.auctionapp.auctionappjava.server.dao.PaymentDeadlineDao;
import com.auctionapp.auctionappjava.server.dao.SecondChanceOfferDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAuctionDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcBidDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcPaymentDeadlineDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcSecondChanceOfferDao;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SecondChanceService {
    private final SecondChanceOfferDao offerDao;
    private final PaymentDeadlineDao paymentDeadlineDao;
    private final AuctionDao auctionDao;
    private final BidDao bidDao;
    private final DeadlinePolicy offerDeadlinePolicy;

    public SecondChanceService() {
        this(new JdbcSecondChanceOfferDao(),
                new JdbcPaymentDeadlineDao(),
                new JdbcAuctionDao(),
                new JdbcBidDao(),
                new FixedDurationDeadlinePolicy(Duration.ofHours(24)));
    }

    public SecondChanceService(SecondChanceOfferDao offerDao,
                               PaymentDeadlineDao paymentDeadlineDao,
                               AuctionDao auctionDao,
                               BidDao bidDao,
                               DeadlinePolicy offerDeadlinePolicy) {
        this.offerDao = offerDao;
        this.paymentDeadlineDao = paymentDeadlineDao;
        this.auctionDao = auctionDao;
        this.bidDao = bidDao;
        this.offerDeadlinePolicy = offerDeadlinePolicy;
    }

    public Optional<SecondChanceOffer> createOfferAfterWinnerFailure(UUID auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay auction: " + auctionId));
        PaymentDeadline deadline = paymentDeadlineDao.findByAuctionId(auctionId)
                .orElseThrow(() -> new IllegalStateException("Auction chua co payment deadline"));

        if (deadline.getStatus() != PaymentDeadlineStatus.FAILED && deadline.getStatus() != PaymentDeadlineStatus.OVERDUE) {
            throw new IllegalStateException("Chi tao second chance khi winner khong thanh toan");
        }

        Optional<BidTransaction> secondHighestBid = findSecondHighestBid(auctionId, auction.getWinnerId());
        if (secondHighestBid.isEmpty()) {
            return Optional.empty();
        }

        BidTransaction bid = secondHighestBid.get();
        SecondChanceOffer offer = new SecondChanceOffer();
        offer.setAuctionId(auctionId);
        offer.setOriginalWinnerId(auction.getWinnerId());
        offer.setOfferedBidderId(bid.getBidderId());
        offer.setOfferAmount(bid.getAmount());
        offer.setExpiresAt(offerDeadlinePolicy.calculateDeadline(auction, LocalDateTime.now()));
        offer.setStatus(SecondChanceOfferStatus.OFFERED);
        offer.setNote("Second chance offer created after winner payment failure.");

        deadline.setStatus(PaymentDeadlineStatus.SECOND_CHANCE_OFFERED);
        deadline.setNote("Second chance offer was created.");
        deadline.touch();
        paymentDeadlineDao.save(deadline);

        return Optional.of(offerDao.save(offer));
    }

    public SecondChanceOffer acceptOffer(UUID offerId) {
        SecondChanceOffer offer = requireOffer(offerId);
        if (offer.isExpired(LocalDateTime.now())) {
            return expireOffer(offerId);
        }
        if (offer.getStatus() != SecondChanceOfferStatus.OFFERED) {
            throw new IllegalStateException("Second chance offer khong con mo");
        }

        offer.setStatus(SecondChanceOfferStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());
        offer.touch();
        SecondChanceOffer savedOffer = offerDao.save(offer);

        auctionDao.findById(offer.getAuctionId()).ifPresent(auction -> {
            auction.setWinnerId(offer.getOfferedBidderId());
            auction.setLeadingBidderId(offer.getOfferedBidderId());
            auction.setCurrentPrice(offer.getOfferAmount());
            auction.setStatus(AuctionStatus.FINISHED);
            auction.touch();
            auctionDao.save(auction);
        });

        return savedOffer;
    }

    public SecondChanceOffer declineOffer(UUID offerId, String note) {
        SecondChanceOffer offer = requireOffer(offerId);
        if (offer.getStatus() != SecondChanceOfferStatus.OFFERED) {
            throw new IllegalStateException("Second chance offer khong con mo");
        }
        offer.setStatus(SecondChanceOfferStatus.DECLINED);
        offer.setRespondedAt(LocalDateTime.now());
        offer.setNote(note);
        offer.touch();
        return offerDao.save(offer);
    }

    public SecondChanceOffer expireOffer(UUID offerId) {
        SecondChanceOffer offer = requireOffer(offerId);
        if (offer.getStatus() != SecondChanceOfferStatus.OFFERED) {
            return offer;
        }
        offer.setStatus(SecondChanceOfferStatus.EXPIRED);
        offer.setRespondedAt(LocalDateTime.now());
        offer.setNote("Second chance offer expired.");
        offer.touch();
        return offerDao.save(offer);
    }

    public List<SecondChanceOffer> expireOpenOffers() {
        List<SecondChanceOffer> expiredOffers = offerDao.findExpiredOpenOffers(LocalDateTime.now());
        for (SecondChanceOffer offer : expiredOffers) {
            offer.setStatus(SecondChanceOfferStatus.EXPIRED);
            offer.setRespondedAt(LocalDateTime.now());
            offer.setNote("Second chance offer expired.");
            offer.touch();
            offerDao.save(offer);
        }
        return expiredOffers;
    }

    public Optional<BidTransaction> findSecondHighestBid(UUID auctionId, UUID originalWinnerId) {
        return bidDao.findByAuctionId(auctionId).stream()
                .filter(bid -> originalWinnerId == null || !originalWinnerId.equals(bid.getBidderId()))
                .sorted(Comparator.comparing(BidTransaction::getAmount, Comparator.nullsLast(BigDecimal::compareTo)).reversed()
                        .thenComparing(BidTransaction::getCreatedAt))
                .findFirst();
    }

    private SecondChanceOffer requireOffer(UUID offerId) {
        return offerDao.findById(offerId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay second chance offer: " + offerId));
    }
}
