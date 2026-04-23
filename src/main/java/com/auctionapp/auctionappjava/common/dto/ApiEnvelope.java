package com.auctionapp.auctionappjava.common.dto;

import com.auctionapp.auctionappjava.common.enums.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class ApiEnvelope<T extends Serializable> implements Serializable {

 @Serial
 private static final long serialVersionUID = 1L;

 private UUID correlationId = UUID.randomUUID();
 private MessageKind kind;
 private RequestAction action;
 private boolean success;
 private String message;
 private T payload;
 private LocalDateTime timestamp = LocalDateTime.now();

 // ================= CONSTRUCTOR =================
 public ApiEnvelope() {}

 private ApiEnvelope(MessageKind kind,
                     RequestAction action,
                     boolean success,
                     String message,
                     T payload) {
  this.kind = kind;
  this.action = action;
  this.success = success;
  this.message = message;
  this.payload = payload;
 }

 // ================= FACTORY METHODS =================
 public static <T extends Serializable> ApiEnvelope<T> request(RequestAction action, T payload) {
  return new ApiEnvelope<>(MessageKind.REQUEST, action, false, null, payload);
 }

 public static <T extends Serializable> ApiEnvelope<T> ok(RequestAction action, String message, T payload) {
  return new ApiEnvelope<>(MessageKind.RESPONSE, action, true, message, payload);
 }

 public static ApiEnvelope<String> fail(RequestAction action, String message) {
  return new ApiEnvelope<>(MessageKind.RESPONSE, action, false, message, message);
 }

 public static <T extends Serializable> ApiEnvelope<T> event(RequestAction action, String message, T payload) {
  return new ApiEnvelope<>(MessageKind.EVENT, action, true, message, payload);
 }

 // ================= GETTER / SETTER =================
 public UUID getCorrelationId() {
  return correlationId;
 }

 public void setCorrelationId(UUID correlationId) {
  this.correlationId = correlationId;
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

 public T getPayload() {
  return payload;
 }

 public void setPayload(T payload) {
  this.payload = payload;
 }

 public LocalDateTime getTimestamp() {
  return timestamp;
 }

 public void setTimestamp(LocalDateTime timestamp) {
  this.timestamp = timestamp;
 }
}