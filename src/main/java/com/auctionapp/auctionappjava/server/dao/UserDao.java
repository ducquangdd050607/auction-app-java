package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.common.model.Wallet;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface UserDao {
    User save(User u);

    Optional<User> findById(UUID id);

    Optional<User> findByName(String name);

    List<User> findAll();

    void updateRole(UUID id, Role role);

    void updateProfile(UUID id, String fullName, String email);

    void updatePassword(UUID id, String hash, String salt);

    void updateActiveStatus(UUID id, boolean isActive);

    Wallet saveWallet(Wallet wallet);

    Optional<Wallet> findWalletByUserId(UUID userId);

    long countUsersActive();
}
