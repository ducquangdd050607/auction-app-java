package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.server.model.Notification;
import java.util.List;
import java.util.UUID;

public interface NotificationDao {

  void createNotification(UUID userId, UUID auctionId, String type, String message);

  List<Notification> findByUserId(UUID userId);

  void deleteByUserId(UUID userId);
}
