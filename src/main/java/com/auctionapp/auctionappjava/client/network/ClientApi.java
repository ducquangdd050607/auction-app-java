package com.auctionapp.auctionappjava.client.network;

import com.auctionapp.auctionappjava.client.session.ClientSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.RequestAction;
import com.auctionapp.auctionappjava.common.model.AutoBidConfig;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientApi {
    private final SocketClient socketClient; private final ClientSession session;
    public ClientApi(SocketClient socketClient, ClientSession session){ this.socketClient=socketClient; this.session=session; }
    public AuthUserDto login(String username, String password){ AuthUserDto u=call(RequestAction.LOGIN, new LoginRequest(username,password), AuthUserDto.class); session.setCurrentUser(u); return u; }
    public AuthUserDto register(RegisterRequest request){ return call(RequestAction.REGISTER, request, AuthUserDto.class); }
    public AuthUserDto selectRole(UUID userId, com.auctionapp.auctionappjava.common.enums.Role role, String adminKey){ AuthUserDto u=call(RequestAction.SELECT_ROLE, new SelectRoleRequest(userId,role,adminKey), AuthUserDto.class); session.setCurrentUser(u); return u; }
    public void logout(){ call(RequestAction.LOGOUT, "OK", String.class); session.clear(); }
    public List<AuctionSummaryDto> listAuctions(){ return callList(RequestAction.LIST_AUCTIONS, "ALL", AuctionSummaryDto.class); }
    public List<AuctionSummaryDto> listMyAuctions(){ return callList(RequestAction.LIST_MY_AUCTIONS, new UserIdRequest(session.getUserId()), AuctionSummaryDto.class); }
    public AuctionDetailDto getAuctionDetail(UUID auctionId){ return call(RequestAction.GET_AUCTION_DETAIL, new AuctionIdRequest(auctionId), AuctionDetailDto.class); }
    public AuctionDetailDto placeBid(UUID auctionId, BigDecimal amount){ return call(RequestAction.PLACE_BID, new PlaceBidRequest(auctionId, session.getUserId(), amount), AuctionDetailDto.class); }
    public AutoBidConfig configureAutoBid(UUID auctionId, BigDecimal maxBid, BigDecimal increment, boolean enabled){ return call(RequestAction.CONFIGURE_AUTO_BID, new AutoBidRequest(auctionId, session.getUserId(), maxBid, increment, enabled), AutoBidConfig.class); }
    public AuctionSummaryDto createAuction(CreateAuctionRequest request){ return call(RequestAction.CREATE_AUCTION, request, AuctionSummaryDto.class); }
    public AuctionSummaryDto updateAuction(UpdateAuctionRequest request){ return call(RequestAction.UPDATE_AUCTION, request, AuctionSummaryDto.class); }
    public WalletDto getWallet(){ return call(RequestAction.GET_WALLET, new UserIdRequest(session.getUserId()), WalletDto.class); }
    public WalletDto deposit(BigDecimal amount){ return call(RequestAction.DEPOSIT, new DepositRequest(session.getUserId(), amount), WalletDto.class); }
    public UserSummaryDto getProfile(){ return call(RequestAction.GET_PROFILE, new UserIdRequest(session.getUserId()), UserSummaryDto.class); }
    public UserSummaryDto updateProfile(String fullName, String email){ return call(RequestAction.UPDATE_PROFILE, new UpdateProfileRequest(session.getUserId(), fullName, email), UserSummaryDto.class); }
    public void changePassword(String oldPassword, String newPassword){ call(RequestAction.CHANGE_PASSWORD, new ChangePasswordRequest(session.getUserId(), oldPassword, newPassword), String.class); }
    public List<UserSummaryDto> listUsers(){ return callList(RequestAction.LIST_USERS, "ALL", UserSummaryDto.class); }
    public AdminOverviewDto adminOverview(){ return call(RequestAction.ADMIN_OVERVIEW, "OK", AdminOverviewDto.class); }
    public void subscribeAuction(UUID auctionId){ call(RequestAction.SUBSCRIBE_AUCTION, new AuctionIdRequest(auctionId), String.class); }
    public void unsubscribeAuction(UUID auctionId){ call(RequestAction.UNSUBSCRIBE_AUCTION, new AuctionIdRequest(auctionId), String.class); }
    public void addAuctionEventListener(Consumer<AuctionEventDto> listener){ socketClient.addEventListener(listener); }
    private <T extends Serializable> T call(RequestAction action, Serializable payload, Class<T> type){ ApiEnvelope<?> response=socketClient.request(ApiEnvelope.request(action, payload)); if(!response.isSuccess()) throw new IllegalStateException(response.getMessage()); return type.cast(response.getPayload()); }
    private <T extends Serializable> List<T> callList(RequestAction action, Serializable payload, Class<T> type){ ApiEnvelope<?> response=socketClient.request(ApiEnvelope.request(action, payload)); if(!response.isSuccess()) throw new IllegalStateException(response.getMessage()); return com.auctionapp.auctionappjava.common.util.JacksonSupport.convertList(response.getPayload(), type); }
}
