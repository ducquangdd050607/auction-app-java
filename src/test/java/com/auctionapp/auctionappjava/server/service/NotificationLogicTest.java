package com.auctionapp.auctionappjava.server.service;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.server.model.Notification;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationLogicTest {

  @Test
  void createNotification_validData_shouldBeRetrievable() {
    // Technique: EP
    TestDaoFakes.FakeNotificationDao dao = new TestDaoFakes.FakeNotificationDao();
    UUID userId = UUID.randomUUID();
    UUID auctionId = UUID.randomUUID();

    dao.createNotification(userId, auctionId, "BID_SUCCESS", "Bid accepted");

    List<Notification> notifications = dao.findByUserId(userId);
    assertEquals(1, notifications.size());
    assertEquals(auctionId, notifications.get(0).getAuctionId());
  }

  @Test
  void createNotification_nullAuctionId_shouldBeRetrievable() {
    // Technique: EP
    TestDaoFakes.FakeNotificationDao dao = new TestDaoFakes.FakeNotificationDao();
    UUID userId = UUID.randomUUID();

    dao.createNotification(userId, null, "WELCOME", "Welcome");

    Notification notification = dao.findByUserId(userId).get(0);
    assertNull(notification.getAuctionId());
  }

  @Test
  void findByUserId_userWithThreeRecords_shouldReturnThree() {
    // Technique: EP
    TestDaoFakes.FakeNotificationDao dao = new TestDaoFakes.FakeNotificationDao();
    UUID userId = UUID.randomUUID();
    dao.createNotification(userId, null, "A", "one");
    dao.createNotification(userId, null, "B", "two");
    dao.createNotification(userId, null, "C", "three");

    assertEquals(3, dao.findByUserId(userId).size());
  }

  @Test
  void findByUserId_userWithoutRecords_shouldReturnEmptyList() {
    // Technique: EP
    TestDaoFakes.FakeNotificationDao dao = new TestDaoFakes.FakeNotificationDao();

    assertTrue(dao.findByUserId(UUID.randomUUID()).isEmpty());
  }

  @Test
  void createNotification_emptyMessage_shouldFollowCurrentBehavior() {
    // Technique: BVA
    // TODO: Service-level validation is not defined; current DAO contract allows empty message.
    TestDaoFakes.FakeNotificationDao dao = new TestDaoFakes.FakeNotificationDao();
    UUID userId = UUID.randomUUID();

    dao.createNotification(userId, null, "INFO", "");

    assertEquals("", dao.findByUserId(userId).get(0).getMessage());
  }

  @Test
  void deleteByUserId_existingRecords_shouldRemoveOnlyThatUser() {
    // Technique: EP
    TestDaoFakes.FakeNotificationDao dao = new TestDaoFakes.FakeNotificationDao();
    UUID userId = UUID.randomUUID();
    UUID otherUserId = UUID.randomUUID();
    dao.createNotification(userId, null, "INFO", "one");
    dao.createNotification(otherUserId, null, "INFO", "two");

    dao.deleteByUserId(userId);

    assertTrue(dao.findByUserId(userId).isEmpty());
    assertEquals(1, dao.findByUserId(otherUserId).size());
  }
}
