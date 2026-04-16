package com.auctionhub.server.dao;

import com.auctionhub.common.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    long count();
}
