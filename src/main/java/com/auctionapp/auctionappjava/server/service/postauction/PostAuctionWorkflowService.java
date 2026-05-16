package com.auctionapp.auctionappjava.server.service.postauction;

import com.auctionapp.auctionappjava.common.model.PaymentDeadline;
import com.auctionapp.auctionappjava.common.model.SecondChanceOffer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostAuctionWorkflowService {
    private final PaymentDeadlineService paymentDeadlineService;
    private final SecondChanceService secondChanceService;

    public PostAuctionWorkflowService() {
        this(new PaymentDeadlineService(), new SecondChanceService());
    }

    public PostAuctionWorkflowService(PaymentDeadlineService paymentDeadlineService, SecondChanceService secondChanceService) {
        this.paymentDeadlineService = paymentDeadlineService;
        this.secondChanceService = secondChanceService;
    }

    public PaymentDeadline openPaymentDeadline(UUID auctionId) {
        return paymentDeadlineService.openDeadlineForFinishedAuction(auctionId);
    }

    public Optional<SecondChanceOffer> failWinnerAndOfferSecondChance(UUID auctionId, String failureNote) {
        paymentDeadlineService.markFailed(auctionId, failureNote);
        return secondChanceService.createOfferAfterWinnerFailure(auctionId);
    }

    public List<PaymentDeadline> markExpiredPaymentDeadlines() {
        return paymentDeadlineService.markExpiredPendingDeadlines();
    }

    public List<SecondChanceOffer> expireSecondChanceOffers() {
        return secondChanceService.expireOpenOffers();
    }
}
