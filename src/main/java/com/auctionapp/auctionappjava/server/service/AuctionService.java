package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.ItemType;
import com.auctionapp.auctionappjava.common.factory.AuctionItemFactory;
import com.auctionapp.auctionappjava.common.model.*;
import com.auctionapp.auctionappjava.server.dao.*;
import com.auctionapp.auctionappjava.server.dao.jdbc.*;

import java.math.BigDecimal;
import java.util.*;

import static com.auctionapp.auctionappjava.common.enums.AuctionStatus.RUNNING;
import static java.time.LocalDateTime.now;

public class AuctionService {
    // Gom tất cả DAO liên quan đến đấu giá vào đây
    private final AuctionDao auctionDao = new JdbcAuctionDao();
    private final AuctionItemDao itemDao = new JdbcAuctionItemDao();
    private final BidDao bidDao = new JdbcBidDao();
    private final UserDao userDao = new JdbcUserDao(); // Cần UserDao để trừ tiền ví

    public Response handleGetAllAuctions() {
        try {
            List<Auction> dbAuctions = auctionDao.findAll();
            List<AuctionSummaryResponse> responseList = new ArrayList<>();

            for (Auction auction : dbAuctions) {
                Optional<Item> itemOpt = itemDao.findById(auction.getItemId());
                if (itemOpt.isPresent()) {
                    Item item = itemOpt.get();
                    int bidderCount = (int) bidDao.countBiddersByAuctionId(auction.getId());

                    responseList.add(new AuctionSummaryResponse(
                            auction.getId().toString(), item.getItemType().name(), item.getTitle(),
                            item.getStartingPrice(), auction.getCurrentPrice(), auction.getMinimumIncrement(),
                            "Đang diễn ra", auction.getStatus(), bidderCount
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
                Optional<Item> itemOpt = itemDao.findById(auction.getItemId());
                if (itemOpt.isPresent()) {
                    Item item = itemOpt.get();
                    int bidderCount = (int) bidDao.countBiddersByAuctionId(auction.getId());

                    responseList.add(new AuctionSummaryResponse(
                            auction.getId().toString(), item.getItemType().name(), item.getTitle(),
                            item.getStartingPrice(), auction.getCurrentPrice(), auction.getMinimumIncrement(),
                            "Đang diễn ra", auction.getStatus(), bidderCount
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
            List<BidTransaction> history = bidDao.findByBidderId(UUID.fromString(data.userId()));

            // Hàm mới của Bình: Cho phép tìm Transaction của riêng Bidders

            List<BidHistoryResponse> responseList = new ArrayList<>();

            // Tạo ArrayList chuẩn bị chuyền dữ liệu vào

            for (BidTransaction bid : history) {
                Optional<Auction> auctionOpt = auctionDao.findById(bid.getAuctionId());

                // Lấy thông tin của Auction qua AuctionId của Cái Transaction

                if (auctionOpt.isPresent()) {
                    Auction auction = auctionOpt.get();

                    Optional<Item> itemOpt = itemDao.findById(auction.getItemId());

                    // Lấy thông tin của Item qua Auction trên

                    if (itemOpt.isPresent()) {
                        Item item = itemOpt.get();
                        responseList.add(new BidHistoryResponse(
                                null,               // Hiện tại chỉ lịch sử của 1 người: You
                                item.getTitle(),               // Lấy tên sản phẩm
                                item.getStartingPrice(),       // Lấy giá bắt đầu
                                bid.getAmount(),               // Lấy giá tiền đã cược
                                RUNNING,                       // WIP
                                "Đang diễn ra"));              // WIP
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
                Optional<Auction> auctionOpt = auctionDao.findById(bid.getAuctionId());
                if (auctionOpt.isPresent()) {
                    Auction auction = auctionOpt.get();

                    Optional<Item> itemOpt = itemDao.findById(auction.getItemId());

                    if (itemOpt.isPresent()) {
                        Item item = itemOpt.get();
                        responseList.add(new BidHistoryResponse(
                                userDao.findById(bid.getBidderId()).get().getFullName(),
                                item.getTitle(),               // Lấy tên sản phẩm
                                item.getStartingPrice(),       // Lấy giá bắt đầu
                                bid.getAmount(),               // Lấy giá tiền đã cược
                                RUNNING,                       // WIP
                                "Đang diễn ra"));              // WIP
                    }
                }
            }
            return new Response(true, "Tải dữ liệu thành công!", responseList);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi truy xuất danh sách!", null);
        }
    }

    public Response handlePlaceBid(PlaceBidRequest placeBidData) {
        try {
            // 1. Kiểm tra phiên đấu giá có tồn tại không
            Optional<Auction> auctionOpt = auctionDao.findById(placeBidData.auctionId());

            if (auctionOpt.isEmpty()) {
                return new Response(false, "Phiên đấu giá không tồn tại!", null);
            } else {
                Auction auction = auctionOpt.get();

                // 2. Validate 1: Phiên đấu giá có đang mở cửa không?
                if (auction.getStatus() != AuctionStatus.OPEN && auction.getStatus() != RUNNING) {
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
                                auctionDao.save(auction);                       // Lưu phiên đấu giá xuống DB

                                return new Response(true, "Đặt giá thành công! Bạn đang dẫn đầu.", null);
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
                    data.attribute2()
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
                    data.openTime(), // Bắt đầu đấu giá ngay lập tức
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

                            Optional<Item> itemOpt = itemDao.findById(auction.getItemId());

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
                        Optional<Item> itemOpt = itemDao.findById(auction.getItemId());
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