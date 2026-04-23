package com.auctionapp.auctionappjava.server.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.auctionapp.auctionappjava.common.enums.AuctionStatus;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.exception.AuthorizationException;
import com.auctionapp.auctionappjava.common.exception.ValidationException;
import com.auctionapp.auctionappjava.common.model.Auction;
import com.auctionapp.auctionappjava.common.model.Bidder;
import com.auctionapp.auctionappjava.common.model.Seller;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.common.model.Wallet;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.dao.WalletDao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

class BidValidationServiceTest {

    @Test
    void sellerCannotBidOnOwnAuction() {
        UUID sellerId = UUID.randomUUID();
        Seller seller = new Seller();
        seller.setId(sellerId);
        seller.setUsername("seller");
        BidValidationService service = new BidValidationService(new InMemoryUserDao(Map.of(sellerId, seller)), new WalletService(new InMemoryWalletDao()));

        assertThrows(AuthorizationException.class, () -> service.validateBid(runningAuction(sellerId), sellerId, new BigDecimal("120")));
    }

    @Test
    void bidMustRespectMinimumIncrement() {
        UUID sellerId = UUID.randomUUID();
        UUID bidderId = UUID.randomUUID();
        Bidder bidder = new Bidder();
        bidder.setId(bidderId);
        bidder.setUsername("bidder");
        InMemoryWalletDao wallets = new InMemoryWalletDao();
        Wallet wallet = new Wallet();
        wallet.setUserId(bidderId);
        wallet.setBalance(new BigDecimal("1000"));
        wallets.save(wallet);
        BidValidationService service = new BidValidationService(new InMemoryUserDao(Map.of(bidderId, bidder)), new WalletService(wallets));

        assertThrows(ValidationException.class, () -> service.validateBid(runningAuction(sellerId), bidderId, new BigDecimal("105")));
    }

    private Auction runningAuction(UUID sellerId) {
        Auction auction = new Auction();
        auction.setSellerId(sellerId);
        auction.setStatus(AuctionStatus.RUNNING);
        auction.setCurrentPrice(new BigDecimal("100"));
        auction.setMinimumIncrement(new BigDecimal("10"));
        auction.setStartTime(LocalDateTime.now().minusMinutes(1));
        auction.setEndTime(LocalDateTime.now().plusMinutes(5));
        return auction;
    }

    private static final class InMemoryUserDao implements UserDao {
        private final Map<UUID, User> users;

        InMemoryUserDao(Map<UUID, User> users) {
            this.users = users;
        }

        @Override public void save(User user) { users.put(user.getId(), user); }
        @Override public void update(User user) { users.put(user.getId(), user); }
        @Override public Optional<User> findById(UUID id) { return Optional.ofNullable(users.get(id)); }
        @Override public Optional<User> findByUsername(String username) { return users.values().stream().filter(u -> username.equals(u.getUsername())).findFirst(); }
        @Override public List<User> findAll() { return new ArrayList<>(users.values()); }
        @Override public long countAll() { return users.size(); }
        @Override public long countByRole(Role role) { return users.values().stream().filter(u -> u.getRole() == role).count(); }
        @Override public void updateActive(UUID userId, boolean active) { users.get(userId).setActive(active); }
    }

    private static final class InMemoryWalletDao implements WalletDao {
        private final Map<UUID, Wallet> walletsByUser = new ConcurrentHashMap<>();

        @Override public void save(Wallet wallet) { walletsByUser.put(wallet.getUserId(), wallet); }
        @Override public void update(Wallet wallet) { walletsByUser.put(wallet.getUserId(), wallet); }
        @Override public Optional<Wallet> findById(UUID id) { return walletsByUser.values().stream().filter(w -> w.getId().equals(id)).findFirst(); }
        @Override public Optional<Wallet> findByUserId(UUID userId) { return Optional.ofNullable(walletsByUser.get(userId)); }
        @Override public List<Wallet> findAll() { return new ArrayList<>(walletsByUser.values()); }
        @Override public void updateBalance(UUID userId, BigDecimal newBalance) { walletsByUser.get(userId).setBalance(newBalance); }
        @Override public void deleteByUserId(UUID userId) { walletsByUser.remove(userId); }
    }
}
