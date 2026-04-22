package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.model.User;

import java.util.*;

public interface UserDao {
    User save(User u);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    void updateRole(UUID id, Role role);

    void updateProfile(UUID id, String fullName, String email);

    void updatePassword(UUID id, String hash, String salt);

    long countAll();
}
