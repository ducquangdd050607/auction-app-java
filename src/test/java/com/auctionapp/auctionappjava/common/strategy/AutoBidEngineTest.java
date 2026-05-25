package com.auctionapp.auctionappjava.common.strategy;

import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.server.model.AutoBidConfig;
import com.auctionapp.auctionappjava.server.strategy.AutoBidEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AutoBidEngine - tinh gia auto-bid")
public class AutoBidEngineTest {

    private final AutoBidEngine engine = new AutoBidEngine();

    @Test
    @DisplayName("Auto-bid co max cao nhat se tu dong vuot nguoi dang dan dau")
    public void testAutoBidOutbidsManualLeader() {
        UUID auctionId = UUID.randomUUID();
        UUID manualLeaderId = UUID.randomUUID();
        UUID autoBidderId = UUID.randomUUID();

        /*
         * Gia hien tai la 120 do nguoi dat tay dang giu.
         * Auto-bidder dat max 200 va buoc nhay 10, nen gia tiep theo can tra la 130.
         */
        AutoBidConfig config = config(auctionId, autoBidderId, "200", "10", true, 1);

        Optional<AutoBidEngine.AutoBidResult> result = engine.calculateNextBid(
                List.of(config),
                new BigDecimal("10"),
                new BigDecimal("120"),
                manualLeaderId
        );

        assertTrue(result.isPresent());
        assertEquals(autoBidderId, result.get().getBidderId());
        assertEquals(new BigDecimal("130"), result.get().getBidAmount());
        assertEquals(new BigDecimal("200"), result.get().getFirstMaxBid());
        assertEquals(new BigDecimal("120"), result.get().getSecondMaxBid());
    }

    @Test
    @DisplayName("Khong tao auto-bid khi chi co mot nguoi hop le")
    public void testNoBidWhenThereIsNoCompetitor() {
        UUID auctionId = UUID.randomUUID();
        UUID autoBidderId = UUID.randomUUID();

        /*
         * Chi co 1 cau hinh auto-bid, chua co nguoi dan dau hien tai.
         * Khong co doi thu de vuot nen engine phai tra ve empty.
         */
        AutoBidConfig config = config(auctionId, autoBidderId, "200", "10", true, 1);

        Optional<AutoBidEngine.AutoBidResult> result = engine.calculateNextBid(
                List.of(config),
                new BigDecimal("10"),
                null,
                null
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Bo qua cau hinh null, bi tat, hoac thieu du lieu bat buoc")
    public void testIgnoreInvalidConfigs() {
        UUID auctionId = UUID.randomUUID();
        UUID manualLeaderId = UUID.randomUUID();
        UUID validBidderId = UUID.randomUUID();

        AutoBidConfig disabled = config(auctionId, UUID.randomUUID(), "500", "10", false, 1);
        AutoBidConfig missingMaxBid = config(auctionId, UUID.randomUUID(), null, "10", true, 2);
        AutoBidConfig valid = config(auctionId, validBidderId, "180", "10", true, 3);

        /*
         * Chi cau hinh valid duoc tinh. Cau hinh disabled/null/missing maxBid bi loai.
         * Valid bidder vuot manual leader tu 120 len 130.
         */
        Optional<AutoBidEngine.AutoBidResult> result = engine.calculateNextBid(
                Arrays.asList(null, disabled, missingMaxBid, valid),
                new BigDecimal("10"),
                new BigDecimal("120"),
                manualLeaderId
        );

        assertTrue(result.isPresent());
        assertEquals(validBidderId, result.get().getBidderId());
        assertEquals(new BigDecimal("130"), result.get().getBidAmount());
    }

    @Test
    @DisplayName("Neu maxBid bang nhau thi nguoi cau hinh truoc duoc uu tien")
    public void testEarlierConfigWinsWhenMaxBidIsEqual() {
        UUID auctionId = UUID.randomUUID();
        UUID firstBidderId = UUID.randomUUID();
        UUID secondBidderId = UUID.randomUUID();

        /*
         * Hai nguoi cung max 200.
         * Engine sap xep theo maxBid giam dan, neu bang nhau thi createdAt som hon dung truoc.
         */
        AutoBidConfig first = config(auctionId, firstBidderId, "200", "10", true, 1);
        AutoBidConfig second = config(auctionId, secondBidderId, "200", "10", true, 2);

        Optional<AutoBidEngine.AutoBidResult> result = engine.calculateNextBid(
                List.of(second, first),
                new BigDecimal("10"),
                new BigDecimal("100"),
                null
        );

        assertTrue(result.isPresent());
        assertEquals(firstBidderId, result.get().getBidderId());
        assertEquals(new BigDecimal("200"), result.get().getBidAmount());
    }

    @Test
    @DisplayName("Neu leader va top 2 cung maxBid thi top 2 auto-bid len maxBid")
    public void testSecondPlaceAutoBidderJumpsToMaxBidWhenMaxBidTiesCurrentLeader() {
        UUID auctionId = UUID.randomUUID();
        UUID leaderId = UUID.randomUUID();
        UUID secondPlaceId = UUID.randomUUID();

        /*
         * Ca hai cung max 1,000,000. Leader dang giu 100,000, top 2 phai nhay auto-bid
         * len thang 1,000,000 de phan thang tie maxBid.
         */
        AutoBidConfig leader = config(auctionId, leaderId, "1000000", "15000", true, 1);
        AutoBidConfig secondPlace = config(auctionId, secondPlaceId, "1000000", "15000", true, 2);

        Optional<AutoBidEngine.AutoBidResult> result = engine.calculateNextBid(
                List.of(leader, secondPlace),
                new BigDecimal("15000"),
                new BigDecimal("100000"),
                leaderId
        );

        assertTrue(result.isPresent());
        assertEquals(secondPlaceId, result.get().getBidderId());
        assertEquals(new BigDecimal("1000000"), result.get().getBidAmount());
        assertEquals(new BigDecimal("1000000"), result.get().getFirstMaxBid());
        assertEquals(new BigDecimal("100000"), result.get().getSecondMaxBid());
    }

    @Test
    @DisplayName("Khong auto-bid khi nguoi dat tay dang co gia cao nhat")
    public void testNoAutoBidWhenManualLeaderIsHighest() {
        UUID auctionId = UUID.randomUUID();
        UUID manualLeaderId = UUID.randomUUID();
        UUID autoBidderId = UUID.randomUUID();

        /*
         * Manual leader dang giu gia 220, cao hon max auto-bid 200.
         * Auto-bid khong du suc vuot nen engine phai tra ve empty.
         */
        AutoBidConfig config = config(auctionId, autoBidderId, "200", "10", true, 1);

        Optional<AutoBidEngine.AutoBidResult> result = engine.calculateNextBid(
                List.of(config),
                new BigDecimal("10"),
                new BigDecimal("220"),
                manualLeaderId
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Buoc gia mac dinh phai lon hon 0")
    public void testDefaultIncrementMustBePositive() {
        /*
         * defaultIncrement la buoc gia toi thieu cua phien dau gia.
         * Neu <= 0 thi dau gia khong co cach tinh gia tiep theo hop le.
         */
        assertThrows(ValidationException.class, () -> engine.calculateNextBid(
                List.of(),
                BigDecimal.ZERO,
                new BigDecimal("100"),
                UUID.randomUUID()
        ));
    }

    private AutoBidConfig config(UUID auctionId,
                                 UUID bidderId,
                                 String maxBid,
                                 String increment,
                                 boolean enabled,
                                 int createdAtSeconds) {
        return new AutoBidConfig(
                UUID.randomUUID(),
                LocalDateTime.of(2026, 1, 1, 10, 0, createdAtSeconds),
                LocalDateTime.of(2026, 1, 1, 10, 0, createdAtSeconds),
                auctionId,
                bidderId,
                maxBid == null ? null : new BigDecimal(maxBid),
                increment == null ? null : new BigDecimal(increment),
                enabled
        );
    }
}
