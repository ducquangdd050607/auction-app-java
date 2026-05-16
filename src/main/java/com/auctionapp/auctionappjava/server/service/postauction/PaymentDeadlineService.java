package com.auctionapp.auctionappjava.server.service.postauction;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.PaymentDeadlineStatus;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.model.PaymentDeadline;
import com.auctionapp.auctionappjava.server.dao.AuctionDao;
import com.auctionapp.auctionappjava.server.dao.PaymentDeadlineDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcAuctionDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcPaymentDeadlineDao;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaymentDeadlineService {
    private final PaymentDeadlineDao paymentDeadlineDao;
    private final AuctionDao auctionDao;
    private final DeadlinePolicy deadlinePolicy;

    public PaymentDeadlineService() {
        this(new JdbcPaymentDeadlineDao(), new JdbcAuctionDao(), new FixedDurationDeadlinePolicy(Duration.ofHours(48)));
    }

    public PaymentDeadlineService(PaymentDeadlineDao paymentDeadlineDao, AuctionDao auctionDao, DeadlinePolicy deadlinePolicy) {
        this.paymentDeadlineDao = paymentDeadlineDao;
        this.auctionDao = auctionDao;
        this.deadlinePolicy = deadlinePolicy;
    }

    public PaymentDeadline openDeadlineForFinishedAuction(UUID auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay auction: " + auctionId));

        if (auction.getStatus() != AuctionStatus.FINISHED) {
            throw new IllegalStateException("Chi tao payment deadline cho auction da ket thuc");
        }
        if (auction.getWinnerId() == null) {
            throw new IllegalStateException("Auction chua co winner");
        }

        Optional<PaymentDeadline> existingDeadline = paymentDeadlineDao.findByAuctionId(auctionId);
        if (existingDeadline.isPresent()) {
            return existingDeadline.get();
        }

        LocalDateTime now = LocalDateTime.now();
        PaymentDeadline deadline = new PaymentDeadline();
        deadline.setAuctionId(auction.getId());
        deadline.setWinnerId(auction.getWinnerId());
        deadline.setAmountDue(auction.getCurrentPrice());
        deadline.setDeadlineAt(deadlinePolicy.calculateDeadline(auction, now));
        deadline.setStatus(PaymentDeadlineStatus.PENDING);
        deadline.setNote("Winner must pay before deadline.");
        return paymentDeadlineDao.save(deadline);
    }

    public PaymentDeadline markPaid(UUID auctionId) {
        PaymentDeadline deadline = requireDeadline(auctionId);
        if (deadline.getStatus() != PaymentDeadlineStatus.PENDING && deadline.getStatus() != PaymentDeadlineStatus.OVERDUE) {
            throw new IllegalStateException("Payment deadline khong con cho thanh toan");
        }

        deadline.setStatus(PaymentDeadlineStatus.PAID);
        deadline.setPaidAt(LocalDateTime.now());
        deadline.touch();
        PaymentDeadline saved = paymentDeadlineDao.save(deadline);

        auctionDao.findById(auctionId).ifPresent(auction -> {
            auction.setStatus(AuctionStatus.PAID);
            auction.touch();
            auctionDao.save(auction);
        });

        return saved;
    }

    public PaymentDeadline markFailed(UUID auctionId, String note) {
        PaymentDeadline deadline = requireDeadline(auctionId);
        if (deadline.getStatus() == PaymentDeadlineStatus.PAID) {
            throw new IllegalStateException("Khong the fail deadline da thanh toan");
        }

        deadline.setStatus(PaymentDeadlineStatus.FAILED);
        deadline.setFailedAt(LocalDateTime.now());
        deadline.setNote(note);
        deadline.touch();
        return paymentDeadlineDao.save(deadline);
    }

    public List<PaymentDeadline> markExpiredPendingDeadlines() {
        LocalDateTime now = LocalDateTime.now();
        List<PaymentDeadline> expiredDeadlines = paymentDeadlineDao.findExpiredPending(now);
        for (PaymentDeadline deadline : expiredDeadlines) {
            deadline.setStatus(PaymentDeadlineStatus.OVERDUE);
            deadline.setNote("Payment deadline is overdue.");
            deadline.touch();
            paymentDeadlineDao.save(deadline);
        }
        return expiredDeadlines;
    }

    public Optional<PaymentDeadline> findByAuctionId(UUID auctionId) {
        return paymentDeadlineDao.findByAuctionId(auctionId);
    }

    private PaymentDeadline requireDeadline(UUID auctionId) {
        return paymentDeadlineDao.findByAuctionId(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("Auction chua co payment deadline: " + auctionId));
    }
}
