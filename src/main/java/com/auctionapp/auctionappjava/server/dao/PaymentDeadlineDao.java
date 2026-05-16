package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.enums.PaymentDeadlineStatus;
import com.auctionapp.auctionappjava.common.model.PaymentDeadline;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentDeadlineDao {
    PaymentDeadline save(PaymentDeadline paymentDeadline);

    Optional<PaymentDeadline> findById(UUID id);

    Optional<PaymentDeadline> findByAuctionId(UUID auctionId);

    List<PaymentDeadline> findByStatus(PaymentDeadlineStatus status);

    List<PaymentDeadline> findExpiredPending(LocalDateTime now);
}
