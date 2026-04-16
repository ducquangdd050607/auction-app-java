package com.auctionhub.server.service;

import com.auctionhub.common.dto.AuthUserDto;
import com.auctionhub.common.dto.LoginRequest;
import com.auctionhub.common.dto.RegisterRequest;
import com.auctionhub.common.enums.Role;
import com.auctionhub.common.exception.AuthException;
import com.auctionhub.common.exception.ValidationException;
import com.auctionhub.common.model.User;
import com.auctionhub.common.util.PasswordUtils;
import com.auctionhub.common.util.ValidationUtils;
import com.auctionhub.server.dao.UserDao;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class AuthService {
    private final UserDao userDao;

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public AuthUserDto login(LoginRequest request) {
        ValidationUtils.requireNotBlank(request.username(), "Tên đăng nhập");
        ValidationUtils.requireNotBlank(request.password(), "Mật khẩu");

        User user = userDao.findByUsername(request.username())
                .orElseThrow(() -> new AuthException("Sai tên đăng nhập hoặc mật khẩu."));

        if (!user.isActive()) {
            throw new AuthException("Tài khoản đang bị khóa.");
        }

        if (!PasswordUtils.verifyPassword(request.password(), user.getPasswordSalt(), user.getPasswordHash())) {
            throw new AuthException("Sai tên đăng nhập hoặc mật khẩu.");
        }
        return toDto(user);
    }

    public AuthUserDto register(RegisterRequest request) {
        ValidationUtils.requireNotBlank(request.username(), "Tên đăng nhập");
        ValidationUtils.requireNotBlank(request.fullName(), "Họ tên");
        ValidationUtils.requireNotBlank(request.password(), "Mật khẩu");
        ValidationUtils.requireValidEmail(request.email());

        if (!Objects.equals(request.password(), request.confirmPassword())) {
            throw new ValidationException("Mật khẩu xác nhận không khớp.");
        }
        if (request.role() == null || request.role() == Role.ADMIN) {
            throw new ValidationException("Chỉ được tự đăng ký vai trò BIDDER hoặc SELLER.");
        }
        if (userDao.findByUsername(request.username()).isPresent()) {
            throw new ValidationException("Tên đăng nhập đã tồn tại.");
        }

        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hashPassword(request.password(), salt);
        User user = com.auctionhub.common.factory.UserFactory.create(
                request.role(),
                UUID.randomUUID(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                request.username(),
                hash,
                salt,
                request.fullName(),
                request.email(),
                true);
        userDao.save(user);
        return toDto(user);
    }

    public AuthUserDto toDto(User user) {
        return new AuthUserDto(user.getId(), user.getUsername(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
