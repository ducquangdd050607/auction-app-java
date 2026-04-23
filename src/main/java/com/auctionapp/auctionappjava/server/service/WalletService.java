package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.WalletDto;
import com.auctionapp.auctionappjava.common.exception.ConflictException;
import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.common.model.Wallet;
import com.auctionapp.auctionappjava.common.util.ValidationUtils;
import com.auctionapp.auctionappjava.server.dao.WalletDao;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WalletService {
    private final WalletDao walletDao;
    private final ConcurrentMap<UUID, Object> locks = new ConcurrentHashMap<>();

    public WalletService(WalletDao walletDao) {
        this.walletDao = walletDao;
    }

    public WalletDto getWallet(UUID userId) {
        Wallet w = getOrCreateWallet(userId);
        return new WalletDto(w.getUserId(), w.getBalance());
    }

    public WalletDto deposit(UUID userId, BigDecimal amount) {
        ValidationUtils.requirePositive(amount, "Số tiền nạp");
        synchronized (lock(userId)) {
            Wallet w = getOrCreateWallet(userId);
            w.setBalance(w.getBalance().add(amount));
            walletDao.update(w);
            return new WalletDto(w.getUserId(), w.getBalance());
        }
    }

    public boolean hasSufficientBalance(UUID userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) return false;
        synchronized (lock(userId)) {
            return getOrCreateWallet(userId).getBalance().compareTo(amount) >= 0;
        }
    }

    public void ensureSufficientBalance(UUID userId, BigDecimal amount) {
        if (!hasSufficientBalance(userId, amount)) throw new ConflictException("Số dư ví không đủ");
    }

    public WalletDto debit(UUID userId, BigDecimal amount) {
        ValidationUtils.requirePositive(amount, "Số tiền trừ");
        synchronized (lock(userId)) {
            Wallet w = getOrCreateWallet(userId);
            if (w.getBalance().compareTo(amount) < 0) throw new ConflictException("Số dư ví không đủ");
            w.setBalance(w.getBalance().subtract(amount));
            walletDao.update(w);
            return new WalletDto(w.getUserId(), w.getBalance());
        }
    }

    public WalletDto credit(UUID userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) return getWallet(userId);
        synchronized (lock(userId)) {
            Wallet w = getOrCreateWallet(userId);
            w.setBalance(w.getBalance().add(amount));
            walletDao.update(w);
            return new WalletDto(w.getUserId(), w.getBalance());
        }
    }

    public Wallet getOrCreateWallet(UUID userId) {
        if (userId == null) throw new ValidationException("UserId không được bỏ trống");
        return walletDao.findByUserId(userId).orElseGet(() -> {
            Wallet w = new Wallet();
            w.setUserId(userId);
            w.setBalance(BigDecimal.ZERO);
            walletDao.save(w);
            return w;
        });
    }

    private Object lock(UUID userId) {
        return locks.computeIfAbsent(userId, ignored -> new Object());
    }
}
