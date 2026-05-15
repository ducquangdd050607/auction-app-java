package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.common.model.*;
import com.auctionapp.auctionappjava.server.dao.*;
import com.auctionapp.auctionappjava.server.dao.jdbc.*;

import com.auctionapp.auctionappjava.common.strategy.AntiSnipingExtensionStrategy;
import com.auctionapp.auctionappjava.server.realtime.AuctionRealtimeHub;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.time.format.DateTimeFormatter;

import static com.auctionapp.auctionappjava.common.enums.AuctionStatus.RUNNING;
import static java.time.LocalDateTime.now;

public class AuctionService {
    // Gom tất cả DAO liên quan đến đấu giá vào đây
    private static final long EXTEND_THRESHOLD_SECONDS = 30;
    private static final long EXTEND_BY_SECONDS = 60;
    private static final ConcurrentHashMap<UUID, ReentrantLock> AUCTION_LOCKS = new ConcurrentHashMap<>();

    private final AuctionDao auctionDao = new JdbcAuctionDao();
    private final AuctionItemDao itemDao = new JdbcAuctionItemDao();
    private final BidDao bidDao = new JdbcBidDao();
    private final UserDao userDao = new JdbcUserDao(); // Cần UserDao để trừ tiền ví
    private final AutoBidDao autoBidDao = new JdbcAutoBidDao();
    private final AutoBidEngine autoBidEngine = new AutoBidEngine();
    private final AntiSnipingExtensionStrategy extensionStrategy =
            new AntiSnipingExtensionStrategy(EXTEND_THRESHOLD_SECONDS, EXTEND_BY_SECONDS);
    private final AuctionRealtimeHub realtimeHub = AuctionRealtimeHub.getInstance();

    public Response handleGetAllAuctions() {
        try {
            List<Auction> dbAuctions = auctionDao.findAll();
            List<AuctionSummaryResponse> responseList = new ArrayList<>();

            for (Auction auction : dbAuctions) {
                Optional<Item> itemOpt = itemDao.findByIdWithoutImage(auction.getItemId());
                if (itemOpt.isPresent()) {
                    Item item = itemOpt.get();
                    int bidderCount = (int) bidDao.countBiddersByAuctionId(auction.getId());

                    responseList.add(new AuctionSummaryResponse(
                            auction.getId().toString(),
                            item.getItemType().name(),
                            item.getTitle(),
                            userDao.findById(auction.getSellerId()).get().getFullName(),
                            item.getDescription(),
                            item.getStartingPrice(),
                            auction.getCurrentPrice(),
                            auction.getMinimumIncrement(),
                            auction.getStartTime(),
                            auction.getEndTime(),
                            0, //TODO: SOON
                            auction.getStatus(),
                            bidderCount,
                            null // Later
                    ));
                }
            }
            return new Response(true, "Tải dữ liệu thành công!", responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
        }
    }

    public Response handleGetAllUploadedAuctions(ManagerAndHistoryRequest data) {
        try {
            List<Auction> dbAuctions = auctionDao.findBySellerId(UUID.fromString(data.userId()));
            List<AuctionSummaryResponse> responseList = new ArrayList<>();

            for (Auction auction : dbAuctions) {
                Optional<Item> itemOpt = itemDao.findByIdWithoutImage(auction.getItemId());
                if (itemOpt.isPresent()) {
                    Item item = itemOpt.get();
                    int bidderCount = (int) bidDao.countBiddersByAuctionId(auction.getId());

                    responseList.add(new AuctionSummaryResponse(
                            auction.getId().toString(),
                            item.getItemType().name(),
                            item.getTitle(),
                            userDao.findById(UUID.fromString(data.userId())).get().getFullName(),
                            item.getDescription(),
                            item.getStartingPrice(),
                            auction.getCurrentPrice(),
                            auction.getMinimumIncrement(),
                            auction.getStartTime(),
                            auction.getEndTime(),
                            0,
                            auction.getStatus(),
                            bidderCount,
                            null
                    ));
                }
            }
            return new Response(true, "Tải dữ liệu thành công!", responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
        }
    }

    public Response handleGetAllPersonalBiddedAuctions(ManagerAndHistoryRequest data) {
        // #FckNowImHungry
        try {
            // Hàm mới của Bình: Cho phép tìm Transaction của riêng Bidders
            List<BidTransaction> history = bidDao.findByBidderId(UUID.fromString(data.userId()));

            // Tạo ArrayList chuẩn bị chuyền dữ liệu vào
            List<BidHistoryResponse> responseList = new ArrayList<>();

            for (BidTransaction bid : history) {
                // Lấy Auction trước
                Optional<Auction> auctionOpt = auctionDao.findById(bid.getAuctionId());

                if (auctionOpt.isPresent()) {
                    Auction auction = auctionOpt.get();

                    // Dùng ItemId của Auction để tìm Item
                    Optional<Item> itemOpt = itemDao.findByIdWithoutImage(auction.getItemId());

                    if (itemOpt.isPresent()) {
                        Item item = itemOpt.get();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                        String formattedTime = bid.getUpdatedAt() != null ? bid.getUpdatedAt().format(formatter) : "Không rõ";

                        responseList.add(new BidHistoryResponse(
                                null,
                                item.getItemType().name(),
                                item.getTitle(),
                                item.getStartingPrice(),
                                bid.getAmount(),
                                auction.getStatus(),
                                formattedTime
                        ));
                    }
                }
            }

            return new Response(true, "Tải dữ liệu thành công!", responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
        }
    }

    public Response handleGetAllBiddedAuctions(ManagerAndHistoryRequest data) {
        try {
            List<BidTransaction> history = bidDao.findAll();

            List<BidHistoryResponse> responseList = new ArrayList<>();

            for (BidTransaction bid : history) {
                // Lấy Auction trước
                Optional<Auction> auctionOpt = auctionDao.findById(bid.getAuctionId());

                if (auctionOpt.isPresent()) {
                    Auction auction = auctionOpt.get();

                    // Dùng ItemId của Auction để tìm Item
                    Optional<Item> itemOpt = itemDao.findByIdWithoutImage(auction.getItemId());

                    if (itemOpt.isPresent()) {
                        Item item = itemOpt.get();

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                        String formattedTime = bid.getUpdatedAt() != null ? bid.getUpdatedAt().format(formatter) : "Không rõ";

                        responseList.add(new BidHistoryResponse(
                                userDao.findById(bid.getBidderId()).get().getFullName(),
                                item.getItemType().name(),
                                item.getTitle(),
                                item.getStartingPrice(),
                                bid.getAmount(),
                                auction.getStatus(),
                                formattedTime
                        ));
                    }
                }
            }

            return new Response(true, "Tải dữ liệu thành công!", responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
        }
    }

    public Response handleGetAllFeaturedAuctions() {
        long counters = 0;

        try {
            List<AuctionSummaryResponse> featuredAuctions = new ArrayList<>();;

            Optional<Auction> auctionOpt = auctionDao.findMostBiddedAuction();

            if (auctionOpt.isPresent()) {
                Auction mostBiddedAuction = auctionOpt.get();
                Optional<Item> itemOpt =  itemDao.findByAuctionId(mostBiddedAuction.getId());
                if (itemOpt.isPresent()) {
                    Item item = itemOpt.get();

                    AuctionSummaryResponse summaryResponse = new AuctionSummaryResponse(
                            mostBiddedAuction.getId().toString(),
                            item.getItemType().name(),
                            item.getTitle(),
                            userDao.findById(mostBiddedAuction.getSellerId()).get().getFullName(),
                            item.getDescription(),
                            item.getStartingPrice(),
                            mostBiddedAuction.getCurrentPrice(),
                            mostBiddedAuction.getMinimumIncrement(),
                            mostBiddedAuction.getStartTime(),
                            mostBiddedAuction.getEndTime(),
                            0,
                            mostBiddedAuction.getStatus(),
                            mostBiddedAuction.getBiddersCount(),
                            null
                            );
                    featuredAuctions.add(summaryResponse);
                }
            }

            return new Response(true, "Tải dữ liệu thành công!", featuredAuctions);

        }
        catch (Exception e) {
                e.printStackTrace();
        }
        return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);

    }

    public Response handlePlaceBid(PlaceBidRequest placeBidData) {
        if (placeBidData == null || placeBidData.auctionId() == null || placeBidData.userId() == null || placeBidData.amount() == null) {
            return new Response(false, "Thiếu dữ liệu đặt giá.", null);
        }

        ReentrantLock lock = lockForAuction(placeBidData.auctionId());
        lock.lock();
        try {
            Optional<Auction> auctionOpt = auctionDao.findById(placeBidData.auctionId());
            if (auctionOpt.isEmpty()) {
                return new Response(false, "Phiên đấu giá không tồn tại!", null);
            }

            Auction auction = auctionOpt.get();
            Response validation = validateAuctionCanBid(auction, placeBidData.amount());
            if (!validation.success()) {
                return validation;
            }

            BidTransaction manualBid = placeBidInternal(
                    auction,
                    placeBidData.userId(),
                    placeBidData.amount(),
                    false,
                    "Giao dịch đặt cược"
            );

            processAutoBids(auction, placeBidData.userId());
            return new Response(true, "Đặt giá thành công! Hệ thống đã cập nhật realtime.", manualBid);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi xử lý đặt giá: " + e.getMessage(), null);
        } finally {
            lock.unlock();
        }
    }

    private ReentrantLock lockForAuction(UUID auctionId) {
        return AUCTION_LOCKS.computeIfAbsent(auctionId, key -> new ReentrantLock());
    }

    private Response validateAuctionCanBid(Auction auction, BigDecimal amount) {
        LocalDateTime currentTime = now();
        if (auction.getStatus().isClosedForBidding()) {
            return new Response(false, "Phiên đấu giá đã đóng.", null);
        }
        if (auction.getStartTime() != null && currentTime.isBefore(auction.getStartTime())) {
            return new Response(false, "Phiên đấu giá chưa bắt đầu.", null);
        }
        if (auction.getEndTime() != null && !currentTime.isBefore(auction.getEndTime())) {
            return new Response(false, "Phiên đấu giá đã kết thúc.", null);
        }
        if (auction.getStatus() == AuctionStatus.OPEN) {
            auction.setStatus(RUNNING);
            auction.touch();
            auctionDao.save(auction);
        }

        BigDecimal minRequiredPrice = auction.getCurrentPrice().add(auction.getMinimumIncrement());
        if (amount.compareTo(minRequiredPrice) < 0) {
            return new Response(false, "Giá đặt phải từ " + minRequiredPrice + " trở lên!", null);
        }
        return new Response(true, "OK", null);
    }

    private BidTransaction placeBidInternal(Auction auction,
                                            UUID bidderId,
                                            BigDecimal amount,
                                            boolean autoGenerated,
                                            String note) {
        Optional<Wallet> currentBidderWalletOpt = userDao.findWalletByUserId(bidderId);
        if (currentBidderWalletOpt.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy ví tiền của người dùng.");
        }
        Wallet currentBidderWallet = currentBidderWalletOpt.get();
        if (availableBidBalance(auction, bidderId, currentBidderWallet).compareTo(amount) < 0) {
            throw new IllegalStateException("Số dư trong ví không đủ để đặt giá.");
        }

        UUID oldLeaderId = auction.getLeadingBidderId();
        BigDecimal oldPrice = auction.getCurrentPrice();
        if (oldLeaderId != null) {
            if (oldLeaderId.equals(bidderId)) {
                currentBidderWallet.setBalance(currentBidderWallet.getBalance().add(oldPrice));
            } else {
                userDao.findWalletByUserId(oldLeaderId).ifPresent(oldLeaderWallet -> {
                    oldLeaderWallet.setBalance(oldLeaderWallet.getBalance().add(oldPrice));
                    oldLeaderWallet.touch();
                    userDao.saveWallet(oldLeaderWallet);
                });
            }
        }

        currentBidderWallet.setBalance(currentBidderWallet.getBalance().subtract(amount));
        currentBidderWallet.touch();
        userDao.saveWallet(currentBidderWallet);

        LocalDateTime bidTime = now();
        BidTransaction bid = new BidTransaction(
                UUID.randomUUID(),
                bidTime,
                bidTime,
                auction.getId(),
                bidderId,
                amount,
                autoGenerated,
                note
        );
        bidDao.save(bid);

        auction.setCurrentPrice(amount);
        auction.setLeadingBidderId(bidderId);
        auction.setBiddersCount((int) bidDao.countBiddersByAuctionId(auction.getId()));
        auction.touch();
        auctionDao.save(auction);

        broadcastBidPlaced(auction, bid);
        applyAntiSnipingIfNeeded(auction, bidTime);
        return bid;
    }

    private BigDecimal availableBidBalance(Auction auction, UUID bidderId, Wallet wallet) {
        BigDecimal available = wallet.getBalance();
        if (bidderId != null && bidderId.equals(auction.getLeadingBidderId())) {
            available = available.add(auction.getCurrentPrice());
        }
        return available;
    }

    private void processAutoBids(Auction auction, UUID manualBidderId) {
        Auction latestAuction = auctionDao.findById(auction.getId()).orElse(auction);
        List<AutoBidConfig> configs = autoBidDao.findEnabledByAuctionId(latestAuction.getId());
        Optional<AutoBidEngine.ProxyBidResult> proxyBidOpt = autoBidEngine.resolveProxyBid(
                configs,
                latestAuction.getCurrentPrice(),
                latestAuction.getMinimumIncrement(),
                latestAuction.getLeadingBidderId()
        );
        if (proxyBidOpt.isEmpty()) {
            return;
        }

        AutoBidEngine.ProxyBidResult proxyBid = proxyBidOpt.get();
        AutoBidConfig winnerConfig = proxyBid.winnerConfig();
        try {
            placeBidInternal(
                    latestAuction,
                    winnerConfig.getBidderId(),
                    proxyBid.finalPrice(),
                    true,
                    "Proxy auto-bid"
            );
            auction.setCurrentPrice(latestAuction.getCurrentPrice());
            auction.setLeadingBidderId(latestAuction.getLeadingBidderId());
            auction.setEndTime(latestAuction.getEndTime());
        } catch (Exception autoBidError) {
            autoBidDao.disableByAuctionIdAndBidderId(winnerConfig.getAuctionId(), winnerConfig.getBidderId());
            System.err.println("[AUTO-BID] Tắt config lỗi: " + autoBidError.getMessage());
        }
    }

    private void applyAntiSnipingIfNeeded(Auction auction, LocalDateTime bidTime) {
        if (auction.getStatus().isClosedForBidding()) return;
        if (!extensionStrategy.shouldExtend(auction, bidTime)) return;

        LocalDateTime newEndTime = extensionStrategy.extendTo(auction, bidTime);
        if (newEndTime.equals(auction.getEndTime())) return;

        auction.setEndTime(newEndTime);
        auction.touch();
        auctionDao.save(auction);
        AuctionStatusService.scheduleAuctionEvents(auction);

        AuctionRealtimeEvent event = new AuctionRealtimeEvent(
                AuctionRealtimeEvent.AUCTION_EXTENDED,
                null,
                auction.getId(),
                null,
                null,
                auction.getCurrentPrice(),
                null,
                auction.getLeadingBidderId(),
                bidTime,
                newEndTime,
                false,
                "Phiên đấu giá được gia hạn do có bid cuối giờ."
        );
        realtimeHub.broadcast(auction.getId(), event);
    }

    private void broadcastBidPlaced(Auction auction, BidTransaction bid) {
        String bidderName = userDao.findById(bid.getBidderId())
                .map(User::getFullName)
                .orElse("Không rõ");

        AuctionRealtimeEvent event = new AuctionRealtimeEvent(
                AuctionRealtimeEvent.BID_PLACED,
                bid.getId(),
                auction.getId(),
                bid.getBidderId(),
                bidderName,
                auction.getCurrentPrice(),
                bid.getAmount(),
                auction.getLeadingBidderId(),
                bid.getCreatedAt(),
                auction.getEndTime(),
                bid.isAutoGenerated(),
                bid.isAutoGenerated() ? "Auto-bid đã được đặt." : "Có bid mới."
        );
        realtimeHub.broadcast(auction.getId(), event);
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
            // 1. Lấy danh sách user
            List<User> dbUsers = userDao.findAll();
            List<UserDetailResponse> responseList = new ArrayList<>();

            // 2. Lặp qua từng user
            for (User user : dbUsers) {
                int counters = 0;
                String latestItemTitle = "";
                if (user.getRole().isBidder()) {
                    // Lấy bid mới nhất của user này
                    Optional<BidTransaction> transactionOpt = bidDao.findLatestBidByBidderId(user.getId());

                    if (transactionOpt.isPresent()) {
                        // Nếu có bid, tìm tên Item tương ứng
                        BidTransaction bid = transactionOpt.get();

                        // Mẫu gốc:
                        // latestItemTitle = itemDao.findById(auctionDao.findById(bid.getAuctionId()).get().getItemId()).get().getTitle();

                        Optional<Auction> auctionOpt = auctionDao.findById(bid.getAuctionId());

                        if (auctionOpt.isPresent()) {
                            Auction auction = auctionOpt.get();

                            Optional<Item> itemOpt = itemDao.findByIdWithoutImage(auction.getItemId());

                            if (itemOpt.isPresent()) {
                                Item item = itemOpt.get();
                                latestItemTitle = item.getTitle();
                                counters = (int) bidDao.countBidsByBidderId(user.getId());
                            } else {
                                // Trường hợp phiên bị xóa.(WIP)
                                latestItemTitle = "Phiên đấu đã bị xóa";
                            }
                        } else {
                            // Nếu không có bid:
                            latestItemTitle = "Acc mới chưa cược";
                        }
                    }
                    // - You messed up again. And again
                    // - C*nt. Not Now

                }

                else if (user.getRole().isSeller()) {
                    Optional<Auction> auctionOpt = auctionDao.findLatestAuctionCreatedBySellerId(user.getId());
                    if (auctionOpt.isPresent()) {
                        Auction auction = auctionOpt.get();
                        Optional<Item> itemOpt = itemDao.findByIdWithoutImage(auction.getItemId());
                        if (itemOpt.isPresent()) {
                            Item item = itemOpt.get();
                            latestItemTitle = item.getTitle();
                            counters = (int) auctionDao.countAuctionsCreatedBySellerId(user.getId());
                        } else {
                            latestItemTitle = "Phiên đấu đã bị xóa";
                        }
                    }

                    else {
                        latestItemTitle = "Acc mới chưa tạo";
                    }
                } else {
                    latestItemTitle = "Admin";
                }

                // Tạo DTO
                UserDetailResponse dto = new UserDetailResponse(
                        String.valueOf(user.getId()),
                        latestItemTitle,
                        user.getFullName(),
                        user.getRole().name(),
                        userDao.findWalletByUserId(user.getId()).get().getBalance(),
                        userDao.findById(user.getId()).get().isActive(),
                        counters
                );
                counters = 0;
                responseList.add(dto);
            }

            return new Response(true, "Tải dữ liệu thành công!", responseList);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
        }
    }

    public Response handleRemoveAuction(RemoveAuctionRequest data) {
        try {
            // Khi ta xóa Item đi, Auction, Transaction cũng bị xóa theo do...
            itemDao.deleteById(itemDao.findByAuctionId(UUID.fromString(data.auctionId())).get().getId());

            return new Response(true, "Đã xóa phiên đấu giá", null);

        } catch(Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);

            // - Good luck.
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
            // - Fuck you whore.
        }
    }
}
