package com.auctionapp.auctionappjava.server.factory;

import static org.junit.jupiter.api.Assertions.*;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.server.model.Admin;
import com.auctionapp.auctionappjava.server.model.Bidder;
import com.auctionapp.auctionappjava.server.model.Seller;
import com.auctionapp.auctionappjava.server.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("UserFactory - tao dung loai User theo Role")
class UserFactoryTest {

  @Test
  @DisplayName("create(ADMIN) phai tra ve instance Admin")
  void create_admin_returnsAdminInstance() {
    User user = UserFactory.create(Role.ADMIN);
    assertInstanceOf(Admin.class, user);
  }

  @Test
  @DisplayName("create(SELLER) phai tra ve instance Seller")
  void create_seller_returnsSellerInstance() {
    User user = UserFactory.create(Role.SELLER);
    assertInstanceOf(Seller.class, user);
  }

  @Test
  @DisplayName("create(BIDDER) phai tra ve instance Bidder")
  void create_bidder_returnsBidderInstance() {
    User user = UserFactory.create(Role.BIDDER);
    assertInstanceOf(Bidder.class, user);
  }

  @Test
  @DisplayName("create(null) phai mac dinh ve Bidder (khong crash)")
  void create_null_defaultsToBidder() {
    // Code gốc: role == null ? Role.BIDDER : role — phải trả về Bidder
    User user = UserFactory.create(null);
    assertNotNull(user, "null không được gây crash");
    assertInstanceOf(
        Bidder.class, user, "null phải mặc định tạo Bidder theo logic trong UserFactory");
  }

  @Test
  @DisplayName("User duoc tao phai co dung Role tuong ung")
  void create_userShouldHaveCorrectRole() {
    assertEquals(Role.ADMIN, UserFactory.create(Role.ADMIN).getRole());
    assertEquals(Role.SELLER, UserFactory.create(Role.SELLER).getRole());
    assertEquals(Role.BIDDER, UserFactory.create(Role.BIDDER).getRole());
  }

  @Test
  @DisplayName("Moi lan goi phai tao ra doi tuong moi (khong dung singleton)")
  void create_shouldReturnNewInstanceEachCall() {
    User user1 = UserFactory.create(Role.BIDDER);
    User user2 = UserFactory.create(Role.BIDDER);
    assertNotSame(user1, user2, "Factory phải tạo instance mới mỗi lần");
  }
}
