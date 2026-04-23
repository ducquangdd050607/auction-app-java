package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.RequestAction;
import com.auctionapp.auctionappjava.common.exception.AppException;
import com.auctionapp.auctionappjava.common.model.AutoBidConfig;
import com.auctionapp.auctionappjava.common.util.JacksonSupport;
import com.auctionapp.auctionappjava.server.service.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class RequestDispatcher {
    private final AuthService authService;
    private final AuctionService auctionService;
    private final WalletService walletService;
    private final UserManagementService userManagementService;
    private final DashboardService dashboardService;
    private final SocketAuctionEventPublisher publisher;

    public RequestDispatcher(AuthService authService, AuctionService auctionService, WalletService walletService, UserManagementService userManagementService, DashboardService dashboardService, SocketAuctionEventPublisher publisher) {
        this.authService = authService;
        this.auctionService = auctionService;
        this.walletService = walletService;
        this.userManagementService = userManagementService;
        this.dashboardService = dashboardService;
        this.publisher = publisher;
    }

    public ApiEnvelope<? extends Serializable> dispatch(ApiEnvelope<?> request, ClientSession session, ClientConnection connection) {
        RequestAction action = request.getAction();
        try {
            Serializable payload = (Serializable) request.getPayload();
            ApiEnvelope<? extends Serializable> response = switch (action) {
                case LOGIN -> {
                    AuthUserDto user = authService.login(JacksonSupport.convertValue(payload, LoginRequest.class));
                    session.login(user);
                    yield ApiEnvelope.ok(action, "Đăng nhập thành công", user);
                }
                case REGISTER ->
                        ApiEnvelope.ok(action, "Đăng ký thành công", authService.register(JacksonSupport.convertValue(payload, RegisterRequest.class)));
                case SELECT_ROLE -> {
                    AuthUserDto user = authService.selectRole(JacksonSupport.convertValue(payload, SelectRoleRequest.class));
                    session.login(user);
                    yield ApiEnvelope.ok(action, "Đã chọn vai trò", user);
                }
                case LOGOUT -> {
                    publisher.unsubscribeAll(connection);
                    session.logout();
                    yield ApiEnvelope.ok(action, "Đăng xuất thành công", "OK");
                }
                case LIST_AUCTIONS ->
                        ApiEnvelope.ok(action, "Danh sách auction", new ArrayList<>(auctionService.listAuctions()));
                case LIST_MY_AUCTIONS ->
                        ApiEnvelope.ok(action, "Auction của tôi", new ArrayList<>(auctionService.listMyAuctions(resolveUserId(payload, session))));
                case GET_AUCTION_DETAIL ->
                        ApiEnvelope.ok(action, "Chi tiết auction", auctionService.getAuctionDetail(JacksonSupport.convertValue(payload, AuctionIdRequest.class).auctionId()));
                case SUBSCRIBE_AUCTION -> {
                    UUID id = JacksonSupport.convertValue(payload, AuctionIdRequest.class).auctionId();
                    publisher.subscribe(id, connection);
                    session.subscribe(id);
                    yield ApiEnvelope.ok(action, "Đã subscribe", "OK");
                }
                case UNSUBSCRIBE_AUCTION -> {
                    UUID id = JacksonSupport.convertValue(payload, AuctionIdRequest.class).auctionId();
                    publisher.unsubscribe(id, connection);
                    session.unsubscribe(id);
                    yield ApiEnvelope.ok(action, "Đã unsubscribe", "OK");
                }
                case PLACE_BID ->
                        ApiEnvelope.ok(action, "Đặt giá thành công", auctionService.placeBid(JacksonSupport.convertValue(payload, PlaceBidRequest.class)));
                case CONFIGURE_AUTO_BID -> {
                    AutoBidConfig cfg = auctionService.configureAutoBid(JacksonSupport.convertValue(payload, AutoBidRequest.class));
                    yield ApiEnvelope.ok(action, "Cập nhật auto bid", cfg);
                }
                case CREATE_AUCTION ->
                        ApiEnvelope.ok(action, "Tạo auction thành công", auctionService.createAuction(JacksonSupport.convertValue(payload, CreateAuctionRequest.class)));
                case UPDATE_AUCTION ->
                        ApiEnvelope.ok(action, "Cập nhật auction thành công", auctionService.updateAuction(JacksonSupport.convertValue(payload, UpdateAuctionRequest.class)));
                case CANCEL_AUCTION ->
                        ApiEnvelope.ok(action, "Hủy auction thành công", auctionService.cancelAuction(JacksonSupport.convertValue(payload, AuctionIdRequest.class).auctionId(), session.getCurrentUserId()));
                case MARK_AUCTION_PAID ->
                        ApiEnvelope.ok(action, "Xác nhận thanh toán", auctionService.markPaid(JacksonSupport.convertValue(payload, AuctionIdRequest.class).auctionId()));
                case DELETE_AUCTION -> {
                    auctionService.deleteAuction(JacksonSupport.convertValue(payload, AuctionIdRequest.class).auctionId(), session.getCurrentUserId());
                    yield ApiEnvelope.ok(action, "Đã xóa auction", "OK");
                }
                case LIST_USERS ->
                        ApiEnvelope.ok(action, "Danh sách user", new ArrayList<>(userManagementService.listUsers()));
                case ADMIN_OVERVIEW -> ApiEnvelope.ok(action, "Admin overview", dashboardService.adminOverview());
                case GET_PROFILE ->
                        ApiEnvelope.ok(action, "Hồ sơ", userManagementService.getProfile(resolveUserId(payload, session)));
                case UPDATE_PROFILE ->
                        ApiEnvelope.ok(action, "Cập nhật hồ sơ", userManagementService.updateProfile(JacksonSupport.convertValue(payload, UpdateProfileRequest.class)));
                case CHANGE_PASSWORD -> {
                    userManagementService.changePassword(JacksonSupport.convertValue(payload, ChangePasswordRequest.class));
                    yield ApiEnvelope.ok(action, "Đã đổi mật khẩu", "OK");
                }
                case GET_WALLET ->
                        ApiEnvelope.ok(action, "Ví", walletService.getWallet(resolveUserId(payload, session)));
                case DEPOSIT -> {
                    DepositRequest d = JacksonSupport.convertValue(payload, DepositRequest.class);
                    yield ApiEnvelope.ok(action, "Nạp tiền thành công", walletService.deposit(d.userId(), d.amount()));
                }
                case SELLER_OVERVIEW ->
                        ApiEnvelope.ok(action, "Seller overview", (Serializable) dashboardService.sellerOverview(resolveUserId(payload, session)));
                case BIDDER_OVERVIEW ->
                        ApiEnvelope.ok(action, "Bidder overview", (Serializable) dashboardService.bidderOverview(resolveUserId(payload, session)));
            };
            response.setCorrelationId(request.getCorrelationId());
            return response;
        } catch (AppException e) {
            ApiEnvelope<String> fail = ApiEnvelope.fail(action, e.getMessage());
            fail.setCorrelationId(request.getCorrelationId());
            return fail;
        } catch (Exception e) {
            ApiEnvelope<String> fail = ApiEnvelope.fail(action, "Lỗi server: " + e.getMessage());
            fail.setCorrelationId(request.getCorrelationId());
            return fail;
        }
    }

    private UUID resolveUserId(Serializable payload, ClientSession session) {
        if (payload instanceof UserIdRequest u && u.userId() != null) return u.userId();
        if (session.getCurrentUserId() != null) return session.getCurrentUserId();
        if (payload instanceof UUID id) return id;
        throw new IllegalArgumentException("Thiếu userId");
    }
}
