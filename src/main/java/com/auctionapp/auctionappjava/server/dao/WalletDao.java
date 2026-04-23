package com.auctionapp.auctionappjava.server.dao;

import com.auctionapp.auctionappjava.common.model.Wallet;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletDao {
    void save(Wallet wallet);
    void update(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    Optional<Wallet> findByUserId(UUID userId);
    List<Wallet> findAll();
    void updateBalance(UUID userId, BigDecimal newBalance);
    void deleteByUserId(UUID userId);
}
