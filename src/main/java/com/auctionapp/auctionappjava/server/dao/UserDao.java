package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    void save(User user);
    void update(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    long countAll();
    long countByRole(Role role);
    void updateActive(UUID userId, boolean active);
}
