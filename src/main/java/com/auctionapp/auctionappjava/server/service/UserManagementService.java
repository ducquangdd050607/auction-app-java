package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.exception.*;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.common.util.PasswordUtils;
import com.auctionapp.auctionappjava.common.util.ValidationUtils;
import com.auctionapp.auctionappjava.server.dao.BidDao;
import com.auctionapp.auctionappjava.server.dao.UserDao;

import java.util.*;

public class UserManagementService {
    private final UserDao userDao;
    private final WalletService walletService;
    private final BidDao bidDao;

    public UserManagementService(UserDao userDao, WalletService walletService, BidDao bidDao) {
        this.userDao = userDao;
        this.walletService = walletService;
        this.bidDao = bidDao;
    }

    public UserSummaryDto getProfile(UUID userId) {
        return toSummary(find(userId));
    }

    public UserSummaryDto updateProfile(UpdateProfileRequest r) {
        if (r == null || r.userId() == null) throw new ValidationException("Thiếu thông tin hồ sơ");
        User u = find(r.userId());
        u.setFullName(ValidationUtils.requireText(r.fullName(), "Họ tên"));
        ValidationUtils.requireEmail(r.email());
        u.setEmail(r.email().trim());
        userDao.update(u);
        return toSummary(u);
    }

    public void changePassword(ChangePasswordRequest r) {
        if (r == null || r.userId() == null) throw new ValidationException("Thiếu thông tin đổi mật khẩu");
        User u = find(r.userId());
        if (!PasswordUtils.verifyPassword(r.oldPassword(), u.getPasswordSalt(), u.getPasswordHash()))
            throw new AuthException("Mật khẩu cũ không đúng");
        String np = ValidationUtils.requireText(r.newPassword(), "Mật khẩu mới");
        if (np.length() < 6) throw new ValidationException("Mật khẩu mới tối thiểu 6 ký tự");
        String salt = PasswordUtils.generateSalt();
        u.setPasswordSalt(salt);
        u.setPasswordHash(PasswordUtils.hashPassword(np, salt));
        userDao.update(u);
    }

    public List<UserSummaryDto> listUsers() {
        List<UserSummaryDto> list = new ArrayList<>();
        for (User u : userDao.findAll()) list.add(toSummary(u));
        return list;
    }

    public UserSummaryDto setActive(UUID userId, boolean active) {
        userDao.updateActive(userId, active);
        return getProfile(userId);
    }

    private User find(UUID id) {
        return userDao.findById(id).orElseThrow(() -> new AuthException("Không tìm thấy user"));
    }

    private UserSummaryDto toSummary(User u) {
        WalletDto w = walletService.getWallet(u.getId());
        return new UserSummaryDto(u.getId(), u.getUsername(), u.getFullName(), u.getEmail(), u.getRole(), u.isActive(), w.balance(), bidDao.countByBidderId(u.getId()));
    }
}
