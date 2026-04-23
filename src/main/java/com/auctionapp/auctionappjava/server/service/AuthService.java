package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.exception.*;
import com.auctionapp.auctionappjava.common.factory.UserFactory;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.common.util.PasswordUtils;
import com.auctionapp.auctionappjava.common.util.ValidationUtils;
import com.auctionapp.auctionappjava.server.dao.UserDao;

import java.util.UUID;

public class AuthService {
    private final UserDao userDao;
    private final WalletService walletService;
    private final String adminKey;

    public AuthService(UserDao userDao, WalletService walletService, String adminKey) {
        this.userDao = userDao;
        this.walletService = walletService;
        this.adminKey = adminKey;
    }

    public AuthUserDto register(RegisterRequest request) {
        if (request == null) throw new ValidationException("Thiếu thông tin đăng ký");
        String username = ValidationUtils.requireText(request.username(), "Tên đăng nhập");
        String password = ValidationUtils.requireText(request.password(), "Mật khẩu");
        String fullName = ValidationUtils.requireText(request.fullName(), "Họ tên");
        ValidationUtils.requireEmail(request.email());
        if (password.length() < 6) throw new ValidationException("Mật khẩu phải có ít nhất 6 ký tự");
        if (userDao.findByUsername(username).isPresent()) throw new ConflictException("Tên đăng nhập đã tồn tại");
        Role role = request.role() == null ? Role.BIDDER : request.role();
        if (role == Role.ADMIN) throw new AuthorizationException("Không thể tự đăng ký ADMIN");
        User user = UserFactory.create(role);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(request.email().trim());
        user.setActive(true);
        String salt = PasswordUtils.generateSalt();
        user.setPasswordSalt(salt);
        user.setPasswordHash(PasswordUtils.hashPassword(password, salt));
        userDao.save(user);
        walletService.getOrCreateWallet(user.getId());
        return toAuthUser(user);
    }

    public AuthUserDto login(LoginRequest request) {
        if (request == null) throw new ValidationException("Thiếu thông tin đăng nhập");
        User user = userDao.findByUsername(ValidationUtils.requireText(request.username(), "Tên đăng nhập")).orElseThrow(() -> new AuthException("Sai tài khoản hoặc mật khẩu"));
        if (!user.isActive()) throw new AuthException("Tài khoản đã bị khóa");
        if (!PasswordUtils.verifyPassword(ValidationUtils.requireText(request.password(), "Mật khẩu"), user.getPasswordSalt(), user.getPasswordHash()))
            throw new AuthException("Sai tài khoản hoặc mật khẩu");
        if (user.getRole() != Role.ADMIN) walletService.getOrCreateWallet(user.getId());
        return toAuthUser(user);
    }

    public AuthUserDto selectRole(SelectRoleRequest request) {
        if (request == null || request.userId() == null || request.role() == null)
            throw new ValidationException("Thiếu thông tin chọn vai trò");
        User user = userDao.findById(request.userId()).orElseThrow(() -> new AuthException("Không tìm thấy user"));
        if (request.role() == Role.ADMIN && (request.adminKey() == null || !request.adminKey().equals(adminKey)))
            throw new AuthorizationException("Mã ADMIN không hợp lệ");
        user.setRole(request.role());
        userDao.update(user);
        if (request.role() != Role.ADMIN) walletService.getOrCreateWallet(user.getId());
        return toAuthUser(user);
    }

    public AuthUserDto getCurrentUser(UUID userId) {
        return userDao.findById(userId).map(this::toAuthUser).orElseThrow(() -> new AuthException("Không tìm thấy user"));
    }

    public AuthUserDto toAuthUser(User user) {
        return new AuthUserDto(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole(), user.isActive());
    }
}
