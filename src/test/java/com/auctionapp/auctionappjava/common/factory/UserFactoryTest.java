package com.auctionapp.auctionappjava.common.factory;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.model.Admin;
import com.auctionapp.auctionappjava.common.model.Bidder;
import com.auctionapp.auctionappjava.common.model.Seller;
import com.auctionapp.auctionappjava.common.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserFactory — tạo đúng loại User theo Role")
class UserFactoryTest {

    @Test
    @DisplayName("create(ADMIN) phải trả về instance Admin")
    void create_admin_returnsAdminInstance() {
        User user = UserFactory.create(Role.ADMIN);
        assertInstanceOf(Admin.class, user);
    }

    @Test
    @DisplayName("create(SELLER) phải trả về instance Seller")
    void create_seller_returnsSellerInstance() {
        User user = UserFactory.create(Role.SELLER);
        assertInstanceOf(Seller.class, user);
    }

    @Test
    @DisplayName("create(BIDDER) phải trả về instance Bidder")
    void create_bidder_returnsBidderInstance() {
        User user = UserFactory.create(Role.BIDDER);
        assertInstanceOf(Bidder.class, user);
    }

    @Test
    @DisplayName("create(null) phải mặc định về Bidder (không crash)")
    void create_null_defaultsToBidder() {
        // Code gốc: role == null ? Role.BIDDER : role — phải trả về Bidder
        User user = UserFactory.create(null);
        assertNotNull(user, "null không được gây crash");
        assertInstanceOf(Bidder.class, user,
                "null phải mặc định tạo Bidder theo logic trong UserFactory");
    }

    @Test
    @DisplayName("User được tạo phải có đúng Role tương ứng")
    void create_userShouldHaveCorrectRole() {
        assertEquals(Role.ADMIN,  UserFactory.create(Role.ADMIN).getRole());
        assertEquals(Role.SELLER, UserFactory.create(Role.SELLER).getRole());
        assertEquals(Role.BIDDER, UserFactory.create(Role.BIDDER).getRole());
    }

    @Test
    @DisplayName("Mỗi lần gọi phải tạo ra đối tượng mới (không dùng singleton)")
    void create_shouldReturnNewInstanceEachCall() {
        User user1 = UserFactory.create(Role.BIDDER);
        User user2 = UserFactory.create(Role.BIDDER);
        assertNotSame(user1, user2, "Factory phải tạo instance mới mỗi lần");
    }
}