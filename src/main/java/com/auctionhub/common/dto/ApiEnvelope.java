package com.auctionhub.common.dto;

import com.auctionhub.common.enums.EventType;
import com.auctionhub.common.enums.MessageKind;
import com.auctionhub.common.enums.RequestAction;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;

public class ApiEnvelope implements Serializable {
    private MessageKind kind;
    private RequestAction action;
    private EventType eventType;
    private String requestId;
    private boolean success;
    private String message;
    private JsonNode payload;

    public ApiEnvelope() {
    }

    public static ApiEnvelope request(RequestAction action, String requestId, JsonNode payload) {
        ApiEnvelope envelope = new ApiEnvelope();
        envelope.kind = MessageKind.REQUEST;
        envelope.action = action;
        envelope.requestId = requestId;
        envelope.payload = payload;
        envelope.success = true;
        return envelope;
    }

    public static ApiEnvelope response(RequestAction action, String requestId, boolean success, String message, JsonNode payload) {
        ApiEnvelope envelope = new ApiEnvelope();
        envelope.kind = MessageKind.RESPONSE;
        envelope.action = action;
        envelope.requestId = requestId;
        envelope.success = success;
        envelope.message = message;
        envelope.payload = payload;
        return envelope;
    }

    public static ApiEnvelope event(EventType eventType, String message, JsonNode payload) {
        ApiEnvelope envelope = new ApiEnvelope();
        envelope.kind = MessageKind.EVENT;
        envelope.eventType = eventType;
        envelope.message = message;
        envelope.success = true;
        envelope.payload = payload;
        return envelope;
    }

    public MessageKind getKind() {
        return kind;
    }

    public void setKind(MessageKind kind) {
        this.kind = kind;
    }

    public RequestAction getAction() {
        return action;
    }

    public void setAction(RequestAction action) {
        this.action = action;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }
}
