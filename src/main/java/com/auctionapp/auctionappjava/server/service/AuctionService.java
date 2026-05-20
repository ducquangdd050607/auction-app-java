package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.client.session.AuctionSession;
import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.common.model.*;
import com.auctionapp.auctionappjava.common.strategy.AntiSnipingExtensionStrategy;
import com.auctionapp.auctionappjava.common.strategy.AutoBidEngine;
import com.auctionapp.auctionappjava.server.dao.*;
import com.auctionapp.auctionappjava.server.dao.jdbc.*;
import com.auctionapp.auctionappjava.server.network.SessionManager;

import java.math.BigDecimal;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.LocalDateTime.now;

public class AuctionService {
    // Gom tất cả DAO liên quan đến đấu giá vào đây
    private final AuctionDao auctionDao = new JdbcAuctionDao();
    private final AuctionItemDao itemDao = new JdbcAuctionItemDao();
    private final BidDao bidDao = new JdbcBidDao();
    // THEM AUTO-BID DAO: dung de luu va doc cau hinh auto-bid.
    private final AutoBidDao autoBidDao = new JdbcAutoBidDao();
    // THEM AUTO-BID ENGINE: tinh nguoi auto-bid top 1 va so tien can tra.
    private final AutoBidEngine autoBidEngine = new AutoBidEngine();
    // THEM ANTI-SNIPING: neu bid sat gio ket thuc thi gia han them thoi gian.
    private final AntiSnipingExtensionStrategy antiSnipingStrategy = new AntiSnipingExtensionStrategy(30, 60);
    private final UserDao userDao = new JdbcUserDao(); // Cần UserDao để trừ tiền ví

    // Thêm khóa luồng cho việc đặt bid
    private static final ConcurrentHashMap<String, Object> auctionLocks = new ConcurrentHashMap<>();

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

            return new Response(true, "Tai bang xep hang thanh cong!", responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Loi may chu khi truy xuat bang xep hang!", null);
        }
    }

    public Response handleGetAllFeaturedAuctions() {

        try {
            List<AuctionSummaryResponse> featuredAuctions = new ArrayList<>();;

            Optional<Auction> auction1Opt = auctionDao.findMostBiddedAuction();

//          Optional<Auction> auction3Opt = auctionDao.findTreadingAuction();

            if (auction1Opt.isPresent()) {
                Auction mostBiddedAuction = auction1Opt.get();
                Optional<Item> item1Opt = itemDao.findByAuctionId(mostBiddedAuction.getId());
                if (item1Opt.isPresent()) {
                    Item item = item1Opt.get();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String startFormattedTime = mostBiddedAuction.getStartTime().format(formatter);
                    String endFormattedTime = mostBiddedAuction.getEndTime().format(formatter);

                    AuctionSummaryResponse summaryResponse1 = new AuctionSummaryResponse(
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
                            null
                    );
                    featuredAuctions.add(summaryResponse1);
                }
            }

            else {
                return new Response(false, "Hiện không có phiên đấu giá nào đang hoạt động", featuredAuctions);
            }

            Optional<Auction> auction2Opt = auctionDao.findMostExpiredAuction();

            if (auction2Opt.isPresent()) {
                Auction mostExpiredAuction = auction2Opt.get();
                Optional<Item> item2Opt = itemDao.findByAuctionId(mostExpiredAuction.getId());
                if (item2Opt.isPresent()) {
                    Item item = item2Opt.get();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    String startFormattedTime = mostExpiredAuction.getStartTime().format(formatter);
                    String endFormattedTime = mostExpiredAuction.getEndTime().format(formatter);

                    AuctionSummaryResponse summaryResponse2 = new AuctionSummaryResponse(
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
                            null
                    );
                    featuredAuctions.add(summaryResponse2);
                }
            }
            return new Response(true, "Tải dữ liệu thành công!", featuredAuctions);
        }
        catch (Exception e) {
                e.printStackTrace();
        }
        return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);

    }

    // THEM AUTO-BID API: luu cau hinh auto-bid ma client gui len.
    public Response handleConfigureAutoBid(ConfigureAutoBidRequest data) {
        try {
            Optional<Auction> auctionOpt = auctionDao.findById(data.auctionId());
            if (auctionOpt.isEmpty()) {
                return new Response(false, "PhiÃªn Ä‘áº¥u giÃ¡ khÃ´ng tá»“n táº¡i!", null);
            }

            Auction auction = auctionOpt.get();
            if (data.maxBid().compareTo(auction.getCurrentPrice()) < 0) {
                return new Response(false, "Max auto-bid pháº£i lá»›n hÆ¡n hoáº·c báº±ng giÃ¡ hiá»‡n táº¡i!", null);
            }

            if (data.incrementAmount().compareTo(auction.getMinimumIncrement()) < 0) {
                return new Response(false, "BÆ°á»›c auto-bid pháº£i tá»« " + auction.getMinimumIncrement() + " trá»Ÿ lÃªn!", null);
            }

            AutoBidConfig config = new AutoBidConfig(
                    UUID.randomUUID(),
                    now(),
                    now(),
                    data.auctionId(),
                    data.bidderId(),
                    data.maxBid(),
                    data.incrementAmount(),
                    data.enabled()
            );
            autoBidDao.save(config);

            return new Response(true, "ÄÃ£ cáº¥u hÃ¬nh auto-bid.", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lá»—i mÃ¡y chá»§ khi cáº¥u hÃ¬nh auto-bid: " + e.getMessage(), null);
        }
    }

    public Response handlePlaceBid(PlaceBidRequest placeBidData) {
        String auctionId = String.valueOf(placeBidData.auctionId());

        // Lấy ổ khóa của riêng phiên đấu giá này ra
        Object roomLock = auctionLocks.computeIfAbsent(auctionId, k -> new Object());

        synchronized (roomLock) {
            try {
                // 1. Kiểm tra phiên đấu giá có tồn tại không
                Optional<Auction> auctionOpt = auctionDao.findById(placeBidData.auctionId());

                if (auctionOpt.isEmpty()) {
                    return new Response(false, "Phiên đấu giá không tồn tại!", null);
                } else {
                    Auction auction = auctionOpt.get();

                    // 2. SO SÁNH GIÁ (Chặn người đến sau nếu giá đã bị người đến trước đẩy lên)
                    if (placeBidData.amount().compareTo(auction.getCurrentPrice()) <= 0) {
                        return new Response(false, "Đã có người nhanh tay hơn đặt giá cao hơn hoặc bằng bạn! Vui lòng làm mới.", null);
                    }

                    // 2. Validate 1: Phiên đấu giá có đang mở cửa không?
                    if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != AuctionStatus.RUNNING) {
                        return new Response(false, "Phiên đấu giá đã kết thúc hoặc chưa bắt đầu!", null);
                    }
                    // 3. Validate 2: Giá đặt có hợp lệ không? (Phải lớn hơn Giá hiện tại + Bước giá tối thiểu)
                    // TODO: Check lại validate 2 này, vì nếu nhớ k nhầm đã xử lí ở controller rồi
                    else {
                        BigDecimal minRequiredPrice = auction.getCurrentPrice().add(auction.getMinimumIncrement());

                        if (placeBidData.amount().compareTo(minRequiredPrice) < 0) {
                            return new Response(false, "Giá đặt phải từ " + minRequiredPrice + " trở lên!", null);
                        } else {
                            // Kiểm tra ví tiền người đặt mới
                            UUID currentBidderId = placeBidData.userId();
                            Optional<Wallet> currentBidderWalletOpt = userDao.findWalletByUserId(currentBidderId);

                            if (currentBidderWalletOpt.isEmpty()) {
                                return new Response(false, "Lỗi: Không tìm thấy ví tiền của người dùng!", null);
                            } else {
                                Wallet currentBidderWallet = currentBidderWalletOpt.get();

                                if (currentBidderWallet.getBalance().compareTo(placeBidData.amount()) < 0) {
                                    // Nếu Số dư < Số tiền muốn đặt
                                    return new Response(false, "Số dư trong ví không đủ để đặt giá!", null);
                                } else {
                                    // Hoàn tiền người dẫn đầu cũ (nếu có)
                                    UUID oldLeaderId = auction.getLeadingBidderId();

                                    if (oldLeaderId != null) {
                                        // Xử lý case hiếm: Người dùng tự bid đè lên chính mình
                                        if (oldLeaderId.equals(currentBidderId)) {
                                            // Hoàn tiền cũ lại vào ví của chính họ
                                            BigDecimal refundedBalance = currentBidderWallet.getBalance().add(auction.getCurrentPrice());
                                            currentBidderWallet.setBalance(refundedBalance);
                                        } else {
                                            // Hoàn tiền cho người khác
                                            Optional<Wallet> oldLeaderWalletOpt = userDao.findWalletByUserId(oldLeaderId);
                                            if (oldLeaderWalletOpt.isPresent()) {
                                                Wallet oldLeaderWallet = oldLeaderWalletOpt.get();
                                                // Cộng trả lại số tiền họ đã cược (chính là currentPrice của phiên hiện tại)
                                                BigDecimal refundedBalance = oldLeaderWallet.getBalance().add(auction.getCurrentPrice());
                                                oldLeaderWallet.setBalance(refundedBalance);
                                                userDao.saveWallet(oldLeaderWallet);

                                                // THÊM MỚI: BÁO CHO NGƯỜI BỊ MẤT TOP LÀ HỌ ĐÃ ĐƯỢC HOÀN TIỀN
                                                Response refundResponse = new Response(true, "SERVER_PUSH_BALANCE", refundedBalance);
                                                SessionManager.getInstance().sendToUser(oldLeaderId.toString(), refundResponse);
                                            }
                                        }
                                    }

                                    // Trừ tiền người đặt mới
                                    BigDecimal updatedBalance = currentBidderWallet.getBalance().subtract(placeBidData.amount());
                                    currentBidderWallet.setBalance(updatedBalance);
                                    userDao.saveWallet(currentBidderWallet);

                                    BidTransaction newBid = new BidTransaction(
                                            UUID.randomUUID(),    // ID tự sinh
                                            now(),  // createdAt
                                            now(),  // updatedAt
                                            placeBidData.auctionId(),     // ID phiên
                                            placeBidData.userId(),        // ID người đặt
                                            placeBidData.amount(),        // Số tiền đặt
                                            false,                // autoGenerated?
                                            "Giao dịch đặt cược"  // Note
                                    );
                                    bidDao.save(newBid);

                                    // Cập nhật lại auction trong database
                                    auction.setCurrentPrice(placeBidData.amount());         // Cập nhật giá cao nhất mới
                                    auction.setLeadingBidderId(placeBidData.userId());      // Cập nhật người đang dẫn đầu
                                    auction.setBiddersCount((int) bidDao.countBiddersByAuctionId(auction.getId()));
                                    // THEM ANTI-SNIPING: bid sat gio ket thuc thi day endTime ra xa hon.
                                    applyAntiSnipingExtension(auction);
                                    auctionDao.save(auction);                       // Lưu phiên đấu giá xuống DB

                                    // THÊM MỚI: BÁO CHO TẤT CẢ BIẾT CÓ GIÁ MỚI
                                    // Đóng gói cả ID phiên và Giá mới vào 1 mảng Object
                                    Object[] pushData = new Object[]{
                                            placeBidData.auctionId(),
                                            placeBidData.amount(),
                                            placeBidData.userId()
                                    };
                                    Response newBidResponse = new Response(true, "SERVER_PUSH_NEW_BID", pushData);
                                    SessionManager.getInstance().broadcast(newBidResponse);

                                    // THEM AUTO-BID ENGINE: sau bid tay, kiem tra cac cau hinh auto-bid va dat gia tu dong neu can.
                                    processAutoBid(auction.getId());

                                    // THEM AUTO-BID BALANCE: doc lai vi sau auto-bid vi user co the vua duoc hoan tien.
                                    BigDecimal finalBalance = userDao.findWalletByUserId(currentBidderId)
                                            .map(Wallet::getBalance)
                                            .orElse(updatedBalance);
                                    // THEM AUTO-BID RESULT: tra ca gia cuoi cung sau khi auto-bid da chay de client khong hien gia cu.
                                    BigDecimal finalAuctionPrice = auctionDao.findById(auction.getId())
                                            .map(Auction::getCurrentPrice)
                                            .orElse(placeBidData.amount());
                                    Object[] resultData = new Object[]{ finalBalance, finalAuctionPrice };
                                    return new Response(true, "Đặt giá thành công!", resultData);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return new Response(false, "Lỗi máy chủ khi xử lý đặt giá: " + e.getMessage(), null);
            }
        }
    }

    // THEM AUTO-BID ENGINE: doc danh sach auto-bid, sap xep maxBid va tao bid tu dong neu hop le.
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
                        auction.getLeadingBidderId()
                );

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

    // THEM AUTO-BID ENGINE: dat gia thay user auto-bid, co tru tien, hoan tien va push realtime.
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
        // THEM AUTO-BID BALANCE: bao cho nguoi duoc auto-bid biet so du da bi tru.
        Response autoBidderBalanceResponse = new Response(true, "SERVER_PUSH_BALANCE", updatedBalance);
        SessionManager.getInstance().sendToUser(bidderId.toString(), autoBidderBalanceResponse);

        BidTransaction autoBid = new BidTransaction(
                UUID.randomUUID(),
                now(),
                now(),
                auction.getId(),
                bidderId,
                amount,
                true,
                "Giao dich auto-bid"
        );
        bidDao.save(autoBid);

        auction.setCurrentPrice(amount);
        auction.setLeadingBidderId(bidderId);
        auction.setBiddersCount((int) bidDao.countBiddersByAuctionId(auction.getId()));
        applyAntiSnipingExtension(auction);
        auctionDao.save(auction);

        Object[] pushData = new Object[]{ auction.getId(), amount, bidderId };
        Response newBidResponse = new Response(true, "SERVER_PUSH_NEW_BID", pushData);
        SessionManager.getInstance().broadcast(newBidResponse);
    }

    // THEM ANTI-SNIPING: neu bid nam trong nguong cuoi gio thi cap nhat endTime moi.
    private void applyAntiSnipingExtension(Auction auction) {
        if (antiSnipingStrategy.shouldExtend(auction, now())) {
            auction.setEndTime(antiSnipingStrategy.extendTo(auction, now()));
        }
    }

    public Response handleAddItem(AddItemRequest data) {
        try {
            // 1. Tạo ID ngẫu nhiên cho vật phẩm mới
            UUID newItemId = UUID.randomUUID();
            UUID sellerId = UUID.fromString(data.sellerId());

            // 2. KHỞI TẠO VÀ LƯU VẬT PHẨM (Sử dụng AuctionItemFactory giống trong JdbcAuctionItemDao)
            Item newItem = AuctionItemFactory.create(
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
                    data.imageData()
            );

            itemDao.save(newItem); // Gọi lệnh INSERT xuống bảng auction_items

            // 3. KHỞI TẠO VÀ LƯU PHIÊN ĐẤU GIÁ LIÊN KẾT VỚI VẬT PHẨM ĐÓ
            Auction newAuction = new Auction(
                    UUID.randomUUID(), // ID phiên đấu giá
                    data.openTime(), // Thời gian tạo
                    data.openTime(), // Thời gian cập nhật
                    newItemId,           // Liên kết khóa ngoại với ID vật phẩm vừa tạo
                    sellerId,          // ID người bán
                    data.startPrice(),   // Giá hiện tại lúc bắt đầu chính là giá khởi điểm
                    null,                // Chưa có ai đấu giá (Leading Bidder = null)
                    data.openTime(), // Bắt đầu đấu giá
                    data.endTime(), // Kết thúc sau X ngày
                    AuctionStatus.OPEN,  // Trạng thái phiên mới tạo là OPEN
                    data.minIncrement(), // Bước giá tối thiểu
                    null                 // Chưa có người chiến thắng
            );

            auctionDao.save(newAuction); // Gọi lệnh INSERT xuống bảng auctions

            // Chủ động đẩy Auction này vào bộ đếm giờ đóng/mở bid
            AuctionStatusService.scheduleAuctionEvents(newAuction);

            // THÊM MỚI: BÁO CHO MỌI NGƯỜI BIẾT CÓ HÀNG MỚI ĐỂ REFRESH LIST
            SessionManager.getInstance().broadcast(new Response(true, "SERVER_PUSH_NEW_AUCTION", null));

            // 4. Báo cáo thành công
            return new Response(true, "Đăng bán sản phẩm thành công! Phiên đấu giá đã được mở.", null);

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
                    return new Response(false, "SELLER không thể xóa các phiên của SELLER khác", null);
                }
            }

            else if (currentUser.getRole() == Role.ADMIN) {
                return new Response(true, "Đã xóa phiên đấu giá thành công", null);
            }

            Optional<Item> itemOpt = itemDao.findByAuctionId(auctionId);
            if (itemOpt.isPresent()) {
                itemDao.deleteById(itemOpt.get().getId());
                return new Response(true, "Đã xóa phiên đấu giá thành công", null);
            } else {
                return new Response(false, "Không tìm thấy vật phẩm của phiên đấu giá này", null);
            }

        } catch (IllegalArgumentException e) {
            return new Response(false, "Định dạng UUID không hợp lệ!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi thực hiện xóa!", null);
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
        }  catch(Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);

            // - Good luck.

        }
    }

    public Response handleGetImage(ImageRequest request) {
        try {
            Optional<byte[]> imageOpt = itemDao.findImageByAuctionId(UUID.fromString(request.auctionId()));

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
}
