package com.auctionapp.auctionappjava.server.service;

import static com.auctionapp.auctionappjava.common.util.MoneyUtils.formatMoney;
import static java.time.LocalDateTime.now;

import com.auctionapp.auctionappjava.common.dto.AddItemRequest;
import com.auctionapp.auctionappjava.common.dto.AuctionSummaryResponse;
import com.auctionapp.auctionappjava.common.dto.AuctionTrendResponse;
import com.auctionapp.auctionappjava.common.dto.BidHistoryResponse;
import com.auctionapp.auctionappjava.common.dto.BidRankingResponse;
import com.auctionapp.auctionappjava.common.dto.ConfigureAutoBidRequest;
import com.auctionapp.auctionappjava.common.dto.ImageRequest;
import com.auctionapp.auctionappjava.common.dto.ImageResponse;
import com.auctionapp.auctionappjava.common.dto.ManagerAndHistoryRequest;
import com.auctionapp.auctionappjava.common.dto.PlaceBidRequest;
import com.auctionapp.auctionappjava.common.dto.RemoveAuctionRequest;
import com.auctionapp.auctionappjava.common.dto.Response;
import com.auctionapp.auctionappjava.common.dto.UserDetailResponse;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.exception.*;
import com.auctionapp.auctionappjava.server.dao.*;
import com.auctionapp.auctionappjava.server.dao.jdbc.*;
import com.auctionapp.auctionappjava.server.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.server.model.Auction;
import com.auctionapp.auctionappjava.server.model.AutoBidConfig;
import com.auctionapp.auctionappjava.server.model.BidTransaction;
import com.auctionapp.auctionappjava.server.model.Item;
import com.auctionapp.auctionappjava.server.model.User;
import com.auctionapp.auctionappjava.server.model.Wallet;
import com.auctionapp.auctionappjava.server.network.SessionManager;
import com.auctionapp.auctionappjava.server.strategy.AntiSnipingExtensionStrategy;
import com.auctionapp.auctionappjava.server.strategy.AutoBidEngine;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionService {
  // Gom tất cả DAO liên quan đến đấu giá vào đây
  private final AuctionDao auctionDao = new JdbcAuctionDao();
  private final AuctionItemDao itemDao = new JdbcAuctionItemDao();
  private final BidDao bidDao = new JdbcBidDao();
  // Thêm AUTO-BID DAO: dùng để lưu và đọc cấu hình auto-bid.
  private final AutoBidDao autoBidDao = new JdbcAutoBidDao();
  // Thêm AUTO-BID ENGINE: tính người auto-bid top 1 và số tiền cần trả.
  private final AutoBidEngine autoBidEngine = new AutoBidEngine();
  // Thêm ANTI-SNIPING: nếu bid sát giờ thì gia hạn thời gian.
  private final AntiSnipingExtensionStrategy antiSnipingStrategy =
      new AntiSnipingExtensionStrategy(30, 60);
  private final UserDao userDao = new JdbcUserDao(); // Cần UserDao để trừ tiền ví
  private final NotificationDao notificationDao = new JdbcNotificationDao();
  // Thêm khóa luồng cho việc đặt bid
  private static final ConcurrentHashMap<String, Object> auctionLocks = new ConcurrentHashMap<>();
  private final AuctionTrendService auctionTrendService = new AuctionTrendService();

  public Response handleGetAllAuctions() {
    try {
      List<AuctionSummaryResponse> responseList = auctionDao.findAllSummaries();
      return new Response(true, "Tải dữ liệu thành công!", responseList);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
    }
  }

  public Response handleGetAllUploadedAuctions(ManagerAndHistoryRequest data) {
    try {
      List<AuctionSummaryResponse> responseList =
          auctionDao.findSummariesBySellerId(UUID.fromString(data.userId()));
      return new Response(true, "Tải dữ liệu thành công!", responseList);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
    }
  }

  public Response handleGetAllPersonalBiddedAuctions(ManagerAndHistoryRequest data) {
    try {
      List<BidHistoryResponse> responseList =
          bidDao.findHistoryByBidderId(UUID.fromString(data.userId()));

      return new Response(true, "Tải dữ liệu thành công!", responseList);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
    }
  }

  public Response handleGetAllBiddedAuctions(ManagerAndHistoryRequest data) {
    try {
      List<BidHistoryResponse> responseList = bidDao.findAllHistory();

      return new Response(true, "Tải dữ liệu thành công!", responseList);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
    }
  }

  public Response handleGetBidRanking(ManagerAndHistoryRequest data) {
    try {
      List<BidRankingResponse> responseList =
          bidDao.findRankingByAuctionId(UUID.fromString(data.userId()));

      return new Response(true, "Tải bảng xếp hạng thành công!", responseList);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Loi may chu khi truy xuat xep hang!", null);
    }
  }

  public Response handleGetAllFeaturedAuctions() {

    try {
      List<AuctionSummaryResponse> featuredAuctions = new ArrayList<>();

      Optional<Auction> auction1Opt = auctionDao.findMostBiddedAuction();

      if (auction1Opt.isPresent()) {
        Auction mostBiddedAuction = auction1Opt.get();
        Optional<Item> item1Opt = itemDao.findByAuctionIdWithoutImage(mostBiddedAuction.getId());
        if (item1Opt.isPresent()) {
          Item item = item1Opt.get();
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
          String startFormattedTime = mostBiddedAuction.getStartTime().format(formatter);
          String endFormattedTime = mostBiddedAuction.getEndTime().format(formatter);

          AuctionSummaryResponse summaryResponse1 =
              new AuctionSummaryResponse(
                  mostBiddedAuction.getId().toString(),
                  item.getItemType().name(),
                  item.getTitle(),
                  userDao.findById(mostBiddedAuction.getSellerId()).get().getFullName(),
                  item.getDescription(),
                  item.getStartingPrice(),
                  mostBiddedAuction.getCurrentPrice(),
                  mostBiddedAuction.getMinimumIncrement(),
                  startFormattedTime,
                  endFormattedTime,
                  0,
                  mostBiddedAuction.getStatus(),
                  mostBiddedAuction.getBiddersCount(),
                  null,
                  0);
          featuredAuctions.add(summaryResponse1);
        }
      }

      Optional<Auction> auction2Opt = auctionDao.findMostExpiredAuction();

      if (auction2Opt.isPresent()) {
        Auction mostExpiredAuction = auction2Opt.get();
        Optional<Item> item2Opt = itemDao.findByAuctionIdWithoutImage(mostExpiredAuction.getId());
        if (item2Opt.isPresent()) {
          Item item = item2Opt.get();

          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
          String startFormattedTime = mostExpiredAuction.getStartTime().format(formatter);
          String endFormattedTime = mostExpiredAuction.getEndTime().format(formatter);

          AuctionSummaryResponse summaryResponse2 =
              new AuctionSummaryResponse(
                  mostExpiredAuction.getId().toString(),
                  item.getItemType().name(),
                  item.getTitle(),
                  userDao.findById(mostExpiredAuction.getSellerId()).get().getFullName(),
                  item.getDescription(),
                  item.getStartingPrice(),
                  mostExpiredAuction.getCurrentPrice(),
                  mostExpiredAuction.getMinimumIncrement(),
                  startFormattedTime,
                  endFormattedTime,
                  0,
                  mostExpiredAuction.getStatus(),
                  mostExpiredAuction.getBiddersCount(),
                  null,
                  0);
          featuredAuctions.add(summaryResponse2);
        }
      }

      Response trendResponse = auctionTrendService.handleGetMostTrendingAuction();
      if (trendResponse.success() && trendResponse.data() != null) {

        AuctionTrendResponse topTrend = (AuctionTrendResponse) trendResponse.data();

        Optional<Auction> auction3Opt = auctionDao.findById(UUID.fromString(topTrend.auctionId()));

        if (auction3Opt.isPresent()) {
          Auction mostTrendingAuction = auction3Opt.get();

          Optional<Item> item2Opt =
              itemDao.findByAuctionIdWithoutImage(mostTrendingAuction.getId());
          if (item2Opt.isPresent()) {
            Item item = item2Opt.get();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String startFormattedTime = mostTrendingAuction.getStartTime().format(formatter);
            String endFormattedTime = mostTrendingAuction.getEndTime().format(formatter);

            AuctionSummaryResponse summaryResponse3 =
                new AuctionSummaryResponse(
                    mostTrendingAuction.getId().toString(),
                    item.getItemType().name(),
                    item.getTitle(),
                    userDao.findById(mostTrendingAuction.getSellerId()).get().getFullName(),
                    item.getDescription(),
                    item.getStartingPrice(),
                    mostTrendingAuction.getCurrentPrice(),
                    mostTrendingAuction.getMinimumIncrement(),
                    startFormattedTime,
                    endFormattedTime,
                    0,
                    mostTrendingAuction.getStatus(),
                    mostTrendingAuction.getBiddersCount(),
                    null,
                    0);
            featuredAuctions.add(summaryResponse3);
          }
        }
      }

      if (featuredAuctions.isEmpty()) {
        return new Response(false, "Hiện không có phiên đấu giá nào nổi bật", featuredAuctions);
      }

      return new Response(true, "Tải dữ liệu thành công!", featuredAuctions);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
  }

  // THÊM AUTO-BID API: lưu cấu hình auto-bid mà client gửi lên.
  public Response handleConfigureAutoBid(ConfigureAutoBidRequest data) {
    try {
      Optional<Auction> auctionOpt = auctionDao.findById(data.auctionId());
      if (auctionOpt.isEmpty()) {
        throw new NotFoundException("Phiên đấu giá không tồn tại!");
      }

      Auction auction = auctionOpt.get();
      if (data.maxBid().compareTo(auction.getCurrentPrice()) < 0) {
        throw new ConflictException("Max auto-bid phai lon hon hoac bang gia hien tai!");
      }

      if (data.incrementAmount().compareTo(auction.getMinimumIncrement()) < 0) {
        throw new ValidationException(
            "Bước auto-bid phải từ " + auction.getMinimumIncrement() + " trở lên!");
      }

      AutoBidConfig config =
          new AutoBidConfig(
              UUID.randomUUID(),
              now(),
              now(),
              data.auctionId(),
              data.bidderId(),
              data.maxBid(),
              data.incrementAmount(),
              data.enabled());
      autoBidDao.save(config);

      return new Response(true, "Đã cấu hình auto-bid.", null);
    } catch (NotFoundException | ConflictException | ValidationException e) {
      return new Response(false, e.getMessage(), null);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Loi may chu khi cau hinh auto-bid!", null);
    }
  }

  public Response handlePlaceBid(PlaceBidRequest placeBidData) {
    try {
      validatePlaceBidRequest(placeBidData);
      String auctionId = String.valueOf(placeBidData.auctionId());

      // Lấy ổ khóa của riêng phiên đấu giá này ra
      Object roomLock = auctionLocks.computeIfAbsent(auctionId, k -> new Object());

      synchronized (roomLock) {
        // 1. Kiểm tra phiên đấu giá có tồn tại không
        Optional<Auction> auctionOpt = auctionDao.findById(placeBidData.auctionId());

        if (auctionOpt.isEmpty()) {
          throw new NotFoundException("Phiên đấu giá không tồn tại!");
        } else {
          Auction auction = auctionOpt.get();

          // Chặn người đến sau nếu giá đã bị người đến trước đẩy lên
          if (placeBidData.amount().compareTo(auction.getCurrentPrice()) <= 0) {
            throw new ConflictException(
                "Đã có người nhanh tay hơn đặt giá cao hơn hoặc bằng bạn! Vui lòng làm mới.");
          }

          if (auction.getStatus() != AuctionStatus.OPEN
              && auction.getStatus() != AuctionStatus.RUNNING) {
            throw new ConflictException("Phiên đấu giá đã kết thúc hoặc chưa bắt đầu!");
          } else if (auction.getEndTime() != null && !auction.getEndTime().isAfter(now())) {
            throw new ConflictException("Phiên đấu giá đã kết thúc!");
          } else {
            BigDecimal minRequiredPrice =
                auction.getCurrentPrice().add(auction.getMinimumIncrement());

            if (placeBidData.amount().compareTo(minRequiredPrice) < 0) {
              throw new ValidationException("Giá đặt phải từ " + minRequiredPrice + " trở lên!");
            } else {
              // Kiểm tra ví tiền người đặt mới
              UUID currentBidderId = placeBidData.userId();
              Optional<Wallet> currentBidderWalletOpt = userDao.findWalletByUserId(currentBidderId);

              if (currentBidderWalletOpt.isEmpty()) {
                throw new NotFoundException("Lỗi: Không tìm thấy ví tiền của người dùng!");
              } else {
                Wallet currentBidderWallet = currentBidderWalletOpt.get();

                // Tính toán số dư thực tế nếu họ tự bid đè lên chính mình
                BigDecimal availableBalance = currentBidderWallet.getBalance();
                UUID oldLeaderId = auction.getLeadingBidderId();

                if (oldLeaderId != null && oldLeaderId.equals(currentBidderId)) {
                  availableBalance = availableBalance.add(auction.getCurrentPrice());
                }

                // Dùng availableBalance để kiểm tra thay vì getBalance() gốc
                if (availableBalance.compareTo(placeBidData.amount()) < 0) {
                  throw new InsufficientBalanceException("Số dư trong ví không đủ để đặt giá!");

                } else {
                  // Hoàn tiền người dẫn đầu cũ (nếu có)
                  if (oldLeaderId != null) {
                    // Xử lý case hiếm: Người dùng tự bid đè lên chính mình
                    if (oldLeaderId.equals(currentBidderId)) {
                      // Hoàn tiền cũ lại vào ví của chính họ
                      BigDecimal refundedBalance =
                          currentBidderWallet.getBalance().add(auction.getCurrentPrice());
                      currentBidderWallet.setBalance(refundedBalance);
                    } else {
                      // Hoàn tiền cho người khác
                      Optional<Wallet> oldLeaderWalletOpt = userDao.findWalletByUserId(oldLeaderId);
                      if (oldLeaderWalletOpt.isPresent()) {
                        Wallet oldLeaderWallet = oldLeaderWalletOpt.get();
                        // Cộng trả lại số tiền họ đã cược (chính là currentPrice của phiên hiện
                        // tại)
                        BigDecimal refundedBalance =
                            oldLeaderWallet.getBalance().add(auction.getCurrentPrice());
                        oldLeaderWallet.setBalance(refundedBalance);
                        userDao.saveWallet(oldLeaderWallet);

                        // THÊM MỚI: BÁO CHO NGƯỜI BỊ MẤT TOP LÀ HỌ ĐÃ ĐƯỢC HOÀN TIỀN
                        Response refundResponse =
                            new Response(true, "SERVER_PUSH_BALANCE", refundedBalance);
                        SessionManager.getInstance()
                            .sendToUser(oldLeaderId.toString(), refundResponse);
                      }
                    }
                  }

                  // Trừ tiền người đặt mới
                  BigDecimal updatedBalance =
                      currentBidderWallet.getBalance().subtract(placeBidData.amount());
                  currentBidderWallet.setBalance(updatedBalance);
                  userDao.saveWallet(currentBidderWallet);

                  BidTransaction newBid =
                      new BidTransaction(
                          UUID.randomUUID(), // ID tự sinh
                          now(), // createdAt
                          now(), // updatedAt
                          placeBidData.auctionId(), // ID phiên
                          placeBidData.userId(), // ID người đặt
                          placeBidData.amount(), // Số tiền đặt
                          false, // autoGenerated?
                          "Giao dịch đặt cược" // Note
                          );
                  bidDao.save(newBid);

                  // Cập nhật lại auction trong database
                  auction.setCurrentPrice(placeBidData.amount()); // Cập nhật giá cao nhất mới
                  auction.setLeadingBidderId(placeBidData.userId()); // Cập nhật người đang dẫn đầu
                  auction.setBiddersCount((int) bidDao.countBiddersByAuctionId(auction.getId()));
                  // THÊM ANTI-SNIPING: bid sát giờ kết thúc thì đẩy endTime ra xa hơn.
                  applyAntiSnipingExtension(auction);
                  auctionDao.save(auction); // Lưu phiên đấu giá xuống DB

                  // Ép kiểu Date ra String để đồng bộ với định dạng hiển thị của Client
                  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                  String newEndTime = auction.getEndTime().format(formatter);

                  // Đóng gói 4 thông tin: [ID phiên, Giá mới, ID người đặt, Tgian kết thúc mới dành
                  // cho anti-snipping]
                  Object[] pushData =
                      new Object[] {
                        placeBidData.auctionId(),
                        placeBidData.amount(),
                        placeBidData.userId(),
                        newEndTime,
                        auction.getBiddersCount()
                      };

                  Response bidResponse = new Response(true, "SERVER_PUSH_NEW_BID", pushData);
                  SessionManager.getInstance().broadcast(bidResponse);

                  // Lấy tên sản phẩm từ Database thông qua itemId
                  String itemName = "một sản phẩm"; // Giá trị mặc định nếu lỗi
                  Optional<Item> itemOpt = itemDao.findById(auction.getItemId());
                  if (itemOpt.isPresent()) {
                    itemName = itemOpt.get().getTitle();
                  }

                  // Thông báo cho chính người đặt giá
                  String bidSuccessMsg =
                      "✅ Đặt giá thành công "
                          + formatMoney(placeBidData.amount())
                          + " VNĐ cho sản phẩm '"
                          + itemName
                          + "'.";
                  notificationDao.createNotification(
                      currentBidderId, auction.getId(), "BID_SUCCESS", bidSuccessMsg);
                  SessionManager.getInstance()
                      .sendToUser(
                          currentBidderId.toString(),
                          new Response(
                              true, "SERVER_PUSH_BID_SUCCESS_NOTIFICATION", bidSuccessMsg));

                  // Thông báo bidder bị đè giá
                  if (oldLeaderId != null && !oldLeaderId.equals(currentBidderId)) {
                    String outbidMessage =
                        "⚠️ Bạn đã bị đè giá ở phiên đấu giá '"
                            + itemName
                            + "'. Hãy vào đặt giá lại ngay để giữ top 1!";
                    notificationDao.createNotification(
                        oldLeaderId, auction.getId(), "OUTBID", outbidMessage);
                    SessionManager.getInstance()
                        .sendToUser(
                            oldLeaderId.toString(),
                            new Response(true, "SERVER_PUSH_OUTBID_NOTIFICATION", outbidMessage));
                  }

                  // Thông báo seller có giá mới
                  UUID sellerId = auction.getSellerId();
                  if (!sellerId.equals(currentBidderId)) {
                    String sellerBidMessage =
                        "🔥 Sản phẩm '"
                            + itemName
                            + "' của bạn vừa có người đặt mức giá mới: "
                            + formatMoney(placeBidData.amount())
                            + " VNĐ.";
                    notificationDao.createNotification(
                        sellerId, auction.getId(), "SELLER_BID", sellerBidMessage);
                    SessionManager.getInstance()
                        .sendToUser(
                            sellerId.toString(),
                            new Response(
                                true, "SERVER_PUSH_SELLER_BID_NOTIFICATION", sellerBidMessage));
                  }

                  // Thông báo cho admin
                  // Lấy tên người dùng trước
                  String bidderName = "Một người dùng";
                  Optional<User> bidderOpt = userDao.findById(currentBidderId);
                  if (bidderOpt.isPresent()) {
                    bidderName = bidderOpt.get().getFullName();
                  }
                  // ...sau đó gửi noti cho admin
                  List<UserDetailResponse> admins =
                      userDao.findAllDetails().stream()
                          .filter(u -> "ADMIN".equals(u.role()))
                          .toList();
                  for (UserDetailResponse admin : admins) {
                    String adminMsg =
                        "🎉 Người dùng "
                            + bidderName
                            + " vừa đặt giá "
                            + formatMoney(placeBidData.amount())
                            + " VNĐ cho sản phẩm '"
                            + itemName
                            + "'.";
                    notificationDao.createNotification(
                        UUID.fromString(admin.userId()), auction.getId(), "ADMIN_SYS", adminMsg);
                    SessionManager.getInstance()
                        .sendToUser(
                            admin.userId(),
                            new Response(true, "SERVER_PUSH_ADMIN_NOTIFICATION", adminMsg));
                  }

                  // THÊM AUTO-BID ENGINE: sau bid tay, kiểm tra các cấu hình auto-bid và đặt giá tự
                  // động nếu cần..
                  processAutoBid(auction.getId());

                  // THÊM AUTO-BID BALANCE: đọc lại ví sau auto-bid vì user có thể vừa được hoàn
                  // tiền..
                  BigDecimal finalBalance =
                      userDao
                          .findWalletByUserId(currentBidderId)
                          .map(Wallet::getBalance)
                          .orElse(updatedBalance);

                  // THÊM AUTO-BID RESULT: trả cả giá cuối cùng sau khi auto-bid đã chạy để client
                  // không hiển thị giá cũ.
                  BigDecimal finalAuctionPrice =
                      auctionDao
                          .findById(auction.getId())
                          .map(Auction::getCurrentPrice)
                          .orElse(placeBidData.amount());

                  // Đếm số lượng Bidder thực tế từ Database
                  int currentBidderCount = (int) bidDao.countBiddersByAuctionId(auction.getId());

                  // Đóng gói 3 thông tin: [Số dư mới, Giá mới, Số lượng Bidder mới]
                  Object[] resultData =
                      new Object[] {finalBalance, finalAuctionPrice, currentBidderCount};
                  return new Response(true, "Đặt giá thành công!", resultData);
                }
              }
            }
          }
        }
      }
    } catch (ConflictException
        | ValidationException
        | NotFoundException
        | InsufficientBalanceException e) {
      return new Response(false, e.getMessage(), null);
    } catch (DatabaseException e) {
      return new Response(false, "Loi du lieu/ket noi khi xu ly dat gia!", null);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi xử lý đặt giá!", null);
    }
  }

  // THÊM AUTO-BID ENGINE: Đọc danh sách cấu hình auto-bid của phiên đấu giá, sắp xếp theo maxBid
  // giảm dần, xác định người có maxBid cao nhất và người đứng thứ hai, sau đó tạo bid tự động nếu
  // hợp lệ.
  private void validatePlaceBidRequest(PlaceBidRequest placeBidData) {
    if (placeBidData == null) {
      throw new ValidationException("Yeu cau dat gia khong hop le");
    }
    if (placeBidData.auctionId() == null) {
      throw new ValidationException("Auction id khong hop le");
    }
    if (placeBidData.userId() == null) {
      throw new ValidationException("User id khong hop le");
    }
    if (placeBidData.amount() == null || placeBidData.amount().compareTo(BigDecimal.ZERO) <= 0) {
      throw new ValidationException("Gia dat phai lon hon 0");
    }
  }

  private void processAutoBid(UUID auctionId) {
    Optional<Auction> auctionOpt = auctionDao.findById(auctionId);
    if (auctionOpt.isEmpty()) {
      return;
    }

    Auction auction = auctionOpt.get();
    List<AutoBidConfig> configs = autoBidDao.findEnabledByAuctionId(auctionId);
    Optional<AutoBidEngine.AutoBidResult> resultOpt =
        autoBidEngine.calculateNextBid(
            configs,
            auction.getMinimumIncrement(),
            auction.getCurrentPrice(),
            auction.getLeadingBidderId());

    if (resultOpt.isEmpty()) {
      return;
    }

    AutoBidEngine.AutoBidResult result = resultOpt.get();
    if (result.getBidderId().equals(auction.getLeadingBidderId())) {
      return;
    }

    BigDecimal minimumNextBid = auction.getCurrentPrice().add(auction.getMinimumIncrement());
    if (result.getBidAmount().compareTo(minimumNextBid) < 0) {
      return;
    }

    placeAutoBid(auction, result.getBidderId(), result.getBidAmount());
  }

  // Thêm AUTO-BID ENGINE: Đặt giá thay cho user bằng auto-bid, xử lý trừ tiền, hoàn tiền cho người
  // bị vượt giá và push realtime.
  private void placeAutoBid(Auction auction, UUID bidderId, BigDecimal amount) {
    Optional<Wallet> bidderWalletOpt = userDao.findWalletByUserId(bidderId);
    if (bidderWalletOpt.isEmpty()) {
      return;
    }

    Wallet bidderWallet = bidderWalletOpt.get();
    if (bidderWallet.getBalance().compareTo(amount) < 0) {
      return;
    }

    UUID oldLeaderId = auction.getLeadingBidderId();
    if (oldLeaderId != null && !oldLeaderId.equals(bidderId)) {
      Optional<Wallet> oldLeaderWalletOpt = userDao.findWalletByUserId(oldLeaderId);
      if (oldLeaderWalletOpt.isPresent()) {
        Wallet oldLeaderWallet = oldLeaderWalletOpt.get();
        BigDecimal refundedBalance = oldLeaderWallet.getBalance().add(auction.getCurrentPrice());
        oldLeaderWallet.setBalance(refundedBalance);
        userDao.saveWallet(oldLeaderWallet);

        Response refundResponse = new Response(true, "SERVER_PUSH_BALANCE", refundedBalance);
        SessionManager.getInstance().sendToUser(oldLeaderId.toString(), refundResponse);
      }
    }

    BigDecimal updatedBalance = bidderWallet.getBalance().subtract(amount);
    bidderWallet.setBalance(updatedBalance);
    userDao.saveWallet(bidderWallet);
    // Thêm AUTO-BID BALANCE: báo cho người đc auto-bid biết số dư đã bị trừ
    Response autoBidderBalanceResponse = new Response(true, "SERVER_PUSH_BALANCE", updatedBalance);
    SessionManager.getInstance().sendToUser(bidderId.toString(), autoBidderBalanceResponse);

    BidTransaction autoBid =
        new BidTransaction(
            UUID.randomUUID(),
            now(),
            now(),
            auction.getId(),
            bidderId,
            amount,
            true,
            "Giao dịch auto-bid");
    bidDao.save(autoBid);

    auction.setCurrentPrice(amount);
    auction.setLeadingBidderId(bidderId);
    auction.setBiddersCount((int) bidDao.countBiddersByAuctionId(auction.getId()));
    applyAntiSnipingExtension(auction);
    auctionDao.save(auction);

    // Update data endtime nếu có anti-snipping
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    String newEndTime = auction.getEndTime().format(formatter);

    Object[] pushData =
        new Object[] {
          auction.getId(),
          amount,
          bidderId,
          newEndTime, // Lấy thêm tgian nếu có anti-snipping
          auction.getBiddersCount()
        };
    Response newBidResponse = new Response(true, "SERVER_PUSH_NEW_BID", pushData);
    SessionManager.getInstance().broadcast(newBidResponse);

    // Lấy tên sản phẩm từ Database thông qua itemId
    String itemName = "một sản phẩm"; // Giá trị mặc định nếu lỗi
    Optional<Item> itemOpt = itemDao.findById(auction.getItemId());
    if (itemOpt.isPresent()) {
      itemName = itemOpt.get().getTitle();
    }

    // Thông báo vừa autobid thành công
    String autoBidSuccessMsg =
        "🤖 Hệ thống vừa tự động nâng giá lên "
            + formatMoney(amount)
            + " VNĐ cho sản phẩm '"
            + itemName
            + "' thay bạn.";
    notificationDao.createNotification(bidderId, auction.getId(), "BID_SUCCESS", autoBidSuccessMsg);
    SessionManager.getInstance()
        .sendToUser(
            bidderId.toString(),
            new Response(true, "SERVER_PUSH_BID_SUCCESS_NOTIFICATION", autoBidSuccessMsg));

    // Thông báo bidder bị đè giá
    if (oldLeaderId != null && !oldLeaderId.equals(bidderId)) {
      String outbidMessage =
          "⚠ Bạn đã bị đè giá ở phiên đấu giá '"
              + itemName
              + "'. Hãy vào đặt giá lại nếu bạn muốn giữ top 1!";
      notificationDao.createNotification(oldLeaderId, auction.getId(), "OUTBID", outbidMessage);
      SessionManager.getInstance()
          .sendToUser(
              oldLeaderId.toString(),
              new Response(true, "SERVER_PUSH_OUTBID_NOTIFICATION", outbidMessage));
    }

    // Thông báo seller có giá mới
    UUID sellerId = auction.getSellerId();
    if (!sellerId.equals(bidderId)) {
      String sellerBidMessage =
          "🔥 Sản phẩm '"
              + itemName
              + "' của bạn vừa có người đặt mức giá mới: "
              + formatMoney(amount)
              + " VNĐ.";
      notificationDao.createNotification(sellerId, auction.getId(), "SELLER_BID", sellerBidMessage);
      SessionManager.getInstance()
          .sendToUser(
              sellerId.toString(),
              new Response(true, "SERVER_PUSH_SELLER_BID_NOTIFICATION", sellerBidMessage));
    }

    // Thông báo cho admin
    // Lấy tên người dùng trước
    String bidderName = "Một người dùng";
    Optional<User> bidderOpt = userDao.findById(bidderId);
    if (bidderOpt.isPresent()) {
      bidderName = bidderOpt.get().getFullName();
    }
    // ...sau đó gửi noti cho admin
    List<UserDetailResponse> admins =
        userDao.findAllDetails().stream().filter(u -> "ADMIN".equals(u.role())).toList();
    for (UserDetailResponse admin : admins) {
      String adminMsg =
          "🎉 Người dùng "
              + bidderName
              + " vừa đặt giá bằng autobid "
              + formatMoney(amount)
              + " VNĐ cho sản phẩm '"
              + itemName
              + "'.";
      notificationDao.createNotification(
          UUID.fromString(admin.userId()), auction.getId(), "ADMIN_SYS", adminMsg);
      SessionManager.getInstance()
          .sendToUser(
              admin.userId(), new Response(true, "SERVER_PUSH_ADMIN_NOTIFICATION", adminMsg));
    }
  }

  // THÊM ANTI-SNIPING.
  private void applyAntiSnipingExtension(Auction auction) {
    if (antiSnipingStrategy.shouldExtend(auction, now())) {
      auction.setEndTime(antiSnipingStrategy.extendTo(auction, now()));
    }
  }

  public Response handleAddItem(AddItemRequest data) {
    try {
      if (data.startPrice() == null || data.startPrice().compareTo(BigDecimal.ZERO) <= 0) {
        throw new ValidationException("Gia khoi diem phai lon hon 0");
      }
      if (data.minIncrement() == null || data.minIncrement().compareTo(BigDecimal.ZERO) <= 0) {
        throw new ValidationException("Buoc gia phai lon hon 0");
      }
      if (data.endTime() == null
          || data.openTime() == null
          || !data.endTime().isAfter(data.openTime())) {
        throw new ValidationException("Thoi gian ket thuc phai sau thoi gian bat dau");
      }
      // 1. Tạo ID ngẫu nhiên cho vật phẩm mới
      UUID newItemId = UUID.randomUUID();
      UUID sellerId = UUID.fromString(data.sellerId());

      // 2. KHỞI TẠO VÀ LƯU VẬT PHẨM (Sử dụng AuctionItemFactory giống trong JdbcAuctionItemDao)
      Item newItem =
          AuctionItemFactory.create(
              ItemType.valueOf(data.itemType().toUpperCase()), // Ép kiểu chuỗi về Enum ItemType
              newItemId,
              data.openTime(),
              data.endTime(),
              sellerId,
              data.title(),
              data.description(),
              data.startPrice(),
              data.attribute1(),
              data.attribute2(),
              data.imageData());

      itemDao.save(newItem); // Gọi lệnh INSERT xuống bảng auction_items

      // 3. KHỞI TẠO VÀ LƯU PHIÊN ĐẤU GIÁ LIÊN KẾT VỚI VẬT PHẨM ĐÓ
      Auction newAuction =
          new Auction(
              UUID.randomUUID(), // ID phiên đấu giá
              data.openTime(), // Thời gian tạo
              data.openTime(), // Thời gian cập nhật
              newItemId, // Liên kết khóa ngoại với ID vật phẩm vừa tạo
              sellerId, // ID người bán
              data.startPrice(), // Giá hiện tại lúc bắt đầu chính là giá khởi điểm
              null, // Chưa có ai đấu giá (Leading Bidder = null)
              data.openTime(), // Bắt đầu đấu giá
              data.endTime(), // Kết thúc sau X ngày
              AuctionStatus.OPEN, // Trạng thái phiên mới tạo là OPEN
              data.minIncrement(), // Bước giá tối thiểu
              null // Chưa có người chiến thắng
              );

      auctionDao.save(newAuction); // Gọi lệnh INSERT xuống bảng auctions

      // Chủ động đẩy Auction này vào bộ đếm giờ đóng/mở bid
      AuctionStatusService.scheduleAuctionEvents(newAuction);

      // THÊM MỚI: BÁO CHO MỌI NGƯỜI BIẾT CÓ HÀNG MỚI ĐỂ REFRESH LIST
      SessionManager.getInstance().broadcast(new Response(true, "SERVER_PUSH_NEW_AUCTION", null));

      // 4. Báo cáo thành công
      return new Response(true, "Đăng bán sản phẩm thành công! Phiên đấu giá đã được mở.", null);

    } catch (ValidationException e) {
      return new Response(false, e.getMessage(), null);
    } catch (IllegalArgumentException e) {
      return new Response(false, "Sai định dạng loại sản phẩm!", null);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi lưu sản phẩm: " + e.getMessage(), null);
    }
  }

  public Response handleGetUsers() {
    try {
      List<UserDetailResponse> responseList = userDao.findAllDetails();
      return new Response(true, "Tải danh sách user thành công!", responseList);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
    }
  }

  public Response handleRemoveAuction(RemoveAuctionRequest data) {
    try {
      validateRemoveAuctionRequest(data);

      UUID userId = UUID.fromString(data.userId());
      UUID auctionId = UUID.fromString(data.auctionId());

      Optional<User> currentUserOpt = userDao.findById(userId);
      if (currentUserOpt.isEmpty()) {
        return new Response(false, "Người dùng không tồn tại!", null);
      }
      User currentUser = currentUserOpt.get();

      if (currentUser.getRole() == Role.SELLER) {
        Optional<User> sellerOpt = userDao.findSellerByAuctionId(auctionId);

        if (sellerOpt.isEmpty() || !sellerOpt.get().getId().equals(currentUser.getId())) {
          throw new AuthorizationException("SELLER không thể xóa các phiên của SELLER khác.");
        }
      }

      Optional<Item> itemOpt = itemDao.findByAuctionId(auctionId);
      if (itemOpt.isPresent()) {
        // Xóa lịch sử bid của phiên đó
        bidDao.deleteByAuctionId(auctionId);
        autoBidDao.deleteByAuctionId(auctionId);

        // Xóa phiên đấu giá
        auctionDao.deleteById(auctionId);

        // Xóa Vật phẩm
        itemDao.deleteById(itemOpt.get().getId());

        // Báo cho các client khác biết để tải lại danh sách
        SessionManager.getInstance().broadcast(new Response(true, "SERVER_PUSH_NEW_AUCTION", null));

        return new Response(true, "Đã xóa phiên đấu giá thành công", null);
      } else {
        return new Response(false, "Không tìm thấy vật phẩm của phiên đấu giá này", null);
      }

    } catch (InvalidRequestException e) {
      return new Response(false, e.getMessage(), null);
    } catch (IllegalArgumentException e) {
      return new Response(false, "Định dạng UUID không hợp lệ!", null);
    } catch (AuthorizationException e) {
      return new Response(false, e.getMessage(), null);
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "lỗi máy chủ khi thực hiện xóa!", null);
    }
  }

  public Response handleSetUserStatus(ManagerAndHistoryRequest data) {
    try {

      Optional<User> user = userDao.findById(UUID.fromString(data.userId()));

      boolean status = false;

      if (user.isPresent()) {
        if (user.get().getRole().isAdmin()) {
          // Không thể chặn Admin
          return new Response(false, "Không thể chặn Admin.", null);
        }
        status = user.get().isActive();
      }

      userDao.updateActiveStatus(UUID.fromString(data.userId()), !status);

      if (status) {
        return new Response(true, "Đã Ban người này", null);
      } else {
        return new Response(true, "Đã Unban người này", null);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
    }
  }

  public Response handleGetImage(ImageRequest request) {
    try {
      Optional<byte[]> imageOpt =
          itemDao.findImageByAuctionId(UUID.fromString(request.auctionId()));

      if (imageOpt.isPresent() && imageOpt.get() != null) {
        return new Response(true, "Tải ảnh thành công", new ImageResponse(imageOpt.get()));
      } else {
        return new Response(false, "Sản phẩm không có ảnh", null);
      }
    } catch (Exception e) {
      e.printStackTrace();
      return new Response(false, "Lỗi máy chủ khi tải ảnh", null);
    }
  }

  private void validateRemoveAuctionRequest(RemoveAuctionRequest data) {
    if (data == null) {
      throw new InvalidRequestException("Yêu cầu xóa phiên đấu giá không hợp lệ.");
    }

    if (data.userId() == null || data.userId().isBlank()) {
      throw new InvalidRequestException("Thiếu userId.");
    }

    if (data.auctionId() == null || data.auctionId().isBlank()) {
      throw new InvalidRequestException("Thiếu auctionId.");
    }
  }
}
