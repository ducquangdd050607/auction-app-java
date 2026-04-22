package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface UserDao {

    // --- Nhóm chức năng Thêm mới & Truy vấn ---

    User save(User u);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    long countAll();

    // --- Nhóm chức năng Cập nhật thông tin ---

    void updateRole(UUID id, Role role);

    void updateProfile(UUID id, String fullName, String email);

    void updatePassword(UUID id, String hash, String salt);
}