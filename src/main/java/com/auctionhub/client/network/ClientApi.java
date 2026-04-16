package com.auctionhub.client.network;

import com.auctionhub.common.dto.AdminOverviewDto;
import com.auctionhub.common.dto.ApiEnvelope;
import com.auctionhub.common.dto.AuctionDetailDto;
import com.auctionhub.common.dto.AuctionIdRequest;
import com.auctionhub.common.dto.AuctionSummaryDto;
import com.auctionhub.common.dto.AuthUserDto;
import com.auctionhub.common.dto.AutoBidRequest;
import com.auctionhub.common.dto.CreateAuctionRequest;
import com.auctionhub.common.dto.LoginRequest;
import com.auctionhub.common.dto.RegisterRequest;
import com.auctionhub.common.dto.UpdateAuctionRequest;
import com.auctionhub.common.enums.EventType;
import com.auctionhub.common.enums.RequestAction;
import com.auctionhub.common.util.JacksonSupport;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class ClientApi {
    private final SocketClient socketClient;

    public ClientApi(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public AuthUserDto login(LoginRequest request) {
        ApiEnvelope response = socketClient.send(RequestAction.LOGIN, request);
        ensureSuccess(response);
        return convert(response.getPayload(), AuthUserDto.class);
    }

    public AuthUserDto register(RegisterRequest request) {
        ApiEnvelope response = socketClient.send(RequestAction.REGISTER, request);
        ensureSuccess(response);
        return convert(response.getPayload(), AuthUserDto.class);
    }

    public void logout() {
        try {
            ApiEnvelope response = socketClient.send(RequestAction.LOGOUT, null);
            ensureSuccess(response);
        } finally {
            socketClient.disconnect();
        }
    }

    public List<AuctionSummaryDto> listAuctions() {
        ApiEnvelope response = socketClient.send(RequestAction.LIST_AUCTIONS, null);
        ensureSuccess(response);
        return convertList(response.getPayload(), AuctionSummaryDto.class);
    }

    public AuctionDetailDto getAuctionDetail(UUID auctionId) {
        ApiEnvelope response = socketClient.send(RequestAction.GET_AUCTION_DETAIL, new AuctionIdRequest(auctionId));
        ensureSuccess(response);
        return convert(response.getPayload(), AuctionDetailDto.class);
    }

    public AuctionDetailDto subscribeAuction(UUID auctionId) {
        ApiEnvelope response = socketClient.send(RequestAction.SUBSCRIBE_AUCTION, new AuctionIdRequest(auctionId));
        ensureSuccess(response);
        return convert(response.getPayload(), AuctionDetailDto.class);
    }

    public void unsubscribeAuction(UUID auctionId) {
        ApiEnvelope response = socketClient.send(RequestAction.UNSUBSCRIBE_AUCTION, new AuctionIdRequest(auctionId));
        ensureSuccess(response);
    }

    public AuctionDetailDto placeBid(com.auctionhub.common.dto.PlaceBidRequest request) {
        ApiEnvelope response = socketClient.send(RequestAction.PLACE_BID, request);
        ensureSuccess(response);
        return convert(response.getPayload(), AuctionDetailDto.class);
    }

    public AuctionDetailDto configureAutoBid(AutoBidRequest request) {
        ApiEnvelope response = socketClient.send(RequestAction.CONFIGURE_AUTO_BID, request);
        ensureSuccess(response);
        return convert(response.getPayload(), AuctionDetailDto.class);
    }

    public AuctionDetailDto createAuction(CreateAuctionRequest request) {
        ApiEnvelope response = socketClient.send(RequestAction.CREATE_AUCTION, request);
        ensureSuccess(response);
        return convert(response.getPayload(), AuctionDetailDto.class);
    }

    public AuctionDetailDto updateAuction(UpdateAuctionRequest request) {
        ApiEnvelope response = socketClient.send(RequestAction.UPDATE_AUCTION, request);
        ensureSuccess(response);
        return convert(response.getPayload(), AuctionDetailDto.class);
    }

    public void deleteAuction(UUID auctionId) {
        ApiEnvelope response = socketClient.send(RequestAction.DELETE_AUCTION, new AuctionIdRequest(auctionId));
        ensureSuccess(response);
    }

    public AuctionDetailDto cancelAuction(UUID auctionId) {
        ApiEnvelope response = socketClient.send(RequestAction.CANCEL_AUCTION, new AuctionIdRequest(auctionId));
        ensureSuccess(response);
        return convert(response.getPayload(), AuctionDetailDto.class);
    }

    public AuctionDetailDto markPaid(UUID auctionId) {
        ApiEnvelope response = socketClient.send(RequestAction.MARK_AUCTION_PAID, new AuctionIdRequest(auctionId));
        ensureSuccess(response);
        return convert(response.getPayload(), AuctionDetailDto.class);
    }

    public List<AuctionSummaryDto> listMyAuctions() {
        ApiEnvelope response = socketClient.send(RequestAction.LIST_MY_AUCTIONS, null);
        ensureSuccess(response);
        return convertList(response.getPayload(), AuctionSummaryDto.class);
    }

    public AdminOverviewDto adminOverview() {
        ApiEnvelope response = socketClient.send(RequestAction.ADMIN_OVERVIEW, null);
        ensureSuccess(response);
        return convert(response.getPayload(), AdminOverviewDto.class);
    }

    public void onEvent(EventType eventType, Consumer<ApiEnvelope> listener) {
        socketClient.onEvent(eventType, listener);
    }

    private void ensureSuccess(ApiEnvelope response) {
        if (!response.isSuccess()) {
            throw new IllegalStateException(response.getMessage());
        }
    }

    private <T> T convert(JsonNode payload, Class<T> type) {
        return JacksonSupport.convert(payload, type);
    }

    private <T> List<T> convertList(JsonNode payload, Class<T> type) {
        return JacksonSupport.mapper().convertValue(payload,
                JacksonSupport.mapper().getTypeFactory().constructCollectionType(List.class, type));
    }
}
