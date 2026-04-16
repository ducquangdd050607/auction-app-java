package com.auctionhub.server.network;

import com.auctionhub.common.dto.AdminOverviewDto;
import com.auctionhub.common.dto.ApiEnvelope;
import com.auctionhub.common.dto.AuctionDetailDto;
import com.auctionhub.common.dto.AuctionIdRequest;
import com.auctionhub.common.dto.AuthUserDto;
import com.auctionhub.common.dto.AutoBidRequest;
import com.auctionhub.common.dto.CreateAuctionRequest;
import com.auctionhub.common.dto.LoginRequest;
import com.auctionhub.common.dto.RegisterRequest;
import com.auctionhub.common.dto.UpdateAuctionRequest;
import com.auctionhub.common.exception.AppException;
import com.auctionhub.common.util.JacksonSupport;
import com.auctionhub.server.service.AuctionService;
import com.auctionhub.server.service.AuthService;


public class RequestDispatcher {
    private final AuthService authService;
    private final AuctionService auctionService;
    private final SocketAuctionEventPublisher publisher;

    public RequestDispatcher(AuthService authService, AuctionService auctionService, SocketAuctionEventPublisher publisher) {
        this.authService = authService;
        this.auctionService = auctionService;
        this.publisher = publisher;
    }

    public ApiEnvelope dispatch(ApiEnvelope request, ClientSession session, com.auctionhub.common.observer.AuctionEventListener listener) {
        try {
            return switch (request.getAction()) {
                case LOGIN -> respond(request, authService.login(JacksonSupport.convert(request.getPayload(), LoginRequest.class)), "Đăng nhập thành công.", session);
                case REGISTER -> respond(request, authService.register(JacksonSupport.convert(request.getPayload(), RegisterRequest.class)), "Đăng ký thành công. Bạn có thể dùng tài khoản này để đăng nhập.", null);
                case LOGOUT -> {
                    session.logout();
                    yield ApiEnvelope.response(request.getAction(), request.getRequestId(), true, "Đăng xuất thành công.", null);
                }
                case LIST_AUCTIONS -> respond(request, auctionService.listAuctions(session.isAuthenticated() ? session.currentUser().id() : null), null, null);
                case GET_AUCTION_DETAIL -> respond(request, auctionService.getAuctionDetail(JacksonSupport.convert(request.getPayload(), AuctionIdRequest.class).auctionId(), session.isAuthenticated() ? session.currentUser().id() : null), null, null);
                case SUBSCRIBE_AUCTION -> {
                    var payload = JacksonSupport.convert(request.getPayload(), AuctionIdRequest.class);
                    publisher.subscribe(payload.auctionId(), listener);
                    AuctionDetailDto detail = auctionService.getAuctionDetail(payload.auctionId(), session.isAuthenticated() ? session.currentUser().id() : null);
                    yield ApiEnvelope.response(request.getAction(), request.getRequestId(), true, "Đã subscribe realtime.", JacksonSupport.toNode(detail));
                }
                case UNSUBSCRIBE_AUCTION -> {
                    var payload = JacksonSupport.convert(request.getPayload(), AuctionIdRequest.class);
                    publisher.unsubscribe(payload.auctionId(), listener);
                    yield ApiEnvelope.response(request.getAction(), request.getRequestId(), true, "Đã hủy subscribe realtime.", null);
                }
                case PLACE_BID -> respond(request, auctionService.placeBid(session.requireUser(), JacksonSupport.convert(request.getPayload(), com.auctionhub.common.dto.PlaceBidRequest.class)), "Bid thành công.", null);
                case CONFIGURE_AUTO_BID -> respond(request, auctionService.configureAutoBid(session.requireUser(), JacksonSupport.convert(request.getPayload(), AutoBidRequest.class)), "Đã cập nhật auto-bid.", null);
                case CREATE_AUCTION -> respond(request, auctionService.createAuction(session.requireUser(), JacksonSupport.convert(request.getPayload(), CreateAuctionRequest.class)), "Tạo phiên đấu giá thành công.", null);
                case UPDATE_AUCTION -> respond(request, auctionService.updateAuction(session.requireUser(), JacksonSupport.convert(request.getPayload(), UpdateAuctionRequest.class)), "Cập nhật phiên đấu giá thành công.", null);
                case DELETE_AUCTION -> {
                    auctionService.deleteAuction(session.requireUser(), JacksonSupport.convert(request.getPayload(), AuctionIdRequest.class).auctionId());
                    yield ApiEnvelope.response(request.getAction(), request.getRequestId(), true, "Đã xoá phiên đấu giá.", null);
                }
                case CANCEL_AUCTION -> respond(request, auctionService.cancelAuction(session.requireUser(), JacksonSupport.convert(request.getPayload(), AuctionIdRequest.class).auctionId()), "Đã hủy phiên đấu giá.", null);
                case MARK_AUCTION_PAID -> respond(request, auctionService.markAuctionPaid(session.requireUser(), JacksonSupport.convert(request.getPayload(), AuctionIdRequest.class).auctionId()), "Đã chuyển trạng thái PAID.", null);
                case LIST_MY_AUCTIONS -> {
                    AuthUserDto actor = session.requireUser();
                    if (actor.role() != com.auctionhub.common.enums.Role.SELLER) {
                        throw new com.auctionhub.common.exception.AuthorizationException("Chỉ seller mới xem được dashboard seller.");
                    }
                    yield respond(request, auctionService.listAuctionsBySeller(actor.id()), null, null);
                }
                case LIST_USERS, ADMIN_OVERVIEW -> {
                    AuthUserDto actor = session.requireUser();
                    if (actor.role() != com.auctionhub.common.enums.Role.ADMIN) {
                        throw new com.auctionhub.common.exception.AuthorizationException("Chỉ admin mới truy cập dashboard quản trị.");
                    }
                    AdminOverviewDto overview = auctionService.adminOverview();
                    yield ApiEnvelope.response(request.getAction(), request.getRequestId(), true, "", JacksonSupport.toNode(overview));
                }
            };
        } catch (AppException ex) {
            return ApiEnvelope.response(request.getAction(), request.getRequestId(), false, ex.getMessage(), null);
        } catch (Exception ex) {
            return ApiEnvelope.response(request.getAction(), request.getRequestId(), false, "Lỗi máy chủ: " + ex.getMessage(), JacksonSupport.toNode(new ErrorPayload(ex.getClass().getSimpleName())));
        }
    }

    private ApiEnvelope respond(ApiEnvelope request, Object payload, String message, ClientSession sessionToLogin) {
        if (sessionToLogin != null && payload instanceof AuthUserDto authUserDto) {
            sessionToLogin.login(authUserDto);
        }
        return ApiEnvelope.response(request.getAction(), request.getRequestId(), true, message, payload == null ? null : JacksonSupport.toNode(payload));
    }

    private record ErrorPayload(String type) {
    }
}
