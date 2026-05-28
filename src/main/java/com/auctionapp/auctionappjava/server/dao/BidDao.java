package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.dto.BidHistoryResponse;
import com.auctionapp.auctionappjava.common.dto.BidRankingResponse;
import com.auctionapp.auctionappjava.server.model.BidTransaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BidDao {
  BidTransaction save(BidTransaction bidTransaction); // lưu lại 1 lượt bid

  List<BidTransaction> findByAuctionId(UUID auctionId); // in tất cả lịch sử trả giá trong 1 phiên

  List<BidTransaction> findByBidderId(UUID bidderId);

  Optional<BidTransaction> findLatestBidByBidderId(UUID bidderId); // Lấy bid mới nhất của bidder

  List<BidTransaction> findAll(); // in tất cả giao dịch của TẤT CẢ bidders(WIP)

  List<BidHistoryResponse> findHistoryByBidderId(UUID bidderId);

  List<BidHistoryResponse> findAllHistory();

  List<BidRankingResponse> findRankingByAuctionId(UUID auctionId);

  long countByAuctionId(UUID auctionId); // đếm số lượng trả giá trong 1 phiên

  long countByBidderId(UUID bidderId); // đếm số lượng đặt bid của 1 người

  long countBiddersByAuctionId(UUID auctionId);

  long countBidsByBidderId(UUID bidderId);

  List<Long> countBidsInWindowTime(
      UUID auctionId, LocalDateTime pastTime, LocalDateTime fromTime, LocalDateTime toTime);

  Optional<BidTransaction> findHighestBidByAuctionId(UUID auctionId);

  void deleteByAuctionId(UUID auctionId); // xóa lịch sử đấu giá của 1 phiên
}
