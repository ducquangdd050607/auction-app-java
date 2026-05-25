package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.dto.UserDetailResponse;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.server.model.User;
import com.auctionapp.auctionappjava.server.model.Wallet;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserDao {
    User save(User u);

    Optional<User> findById(UUID id);

    Optional<User> findByName(String name);

    Optional<User> findSellerByAuctionId(UUID auctionId);

    List<UserDetailResponse> findAllDetails(); // tải lên danh sách người dùng với đầy đủ thông tin cho admin xem
                                               // chỉ cần trong 1 lần truy vấn

    void updateRole(UUID id, Role role);

    void updateProfile(UUID id, String fullName, String email);

    void updatePassword(UUID id, String hash, String salt);

    void updateActiveStatus(UUID id, boolean isActive);

    Wallet saveWallet(Wallet wallet);

    Optional<Wallet> findWalletByUserId(UUID userId);

    long countUsersActive();
}
