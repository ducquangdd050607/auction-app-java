package com.auctionapp.auctionappjava.server.service;

import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.factory.UserFactory;
import com.auctionapp.auctionappjava.common.model.User;
import com.auctionapp.auctionappjava.common.model.Wallet;
import com.auctionapp.auctionappjava.common.util.PasswordUtils;
import com.auctionapp.auctionappjava.server.dao.UserDao;
import com.auctionapp.auctionappjava.server.dao.jdbc.JdbcUserDao;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static java.time.LocalDateTime.now;

public class UserService {
    // Service sẽ ôm các DAO tương ứng
    private final UserDao userDao = new JdbcUserDao();

    public Response handleLogin(LoginRequest loginData) {
        try {
            Optional<User> userOptional = userDao.findByName(loginData.username());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                if (PasswordUtils.verifyPassword(loginData.password(), user.getPasswordSalt(), user.getPasswordHash())) {
                    BigDecimal balance = userDao.findWalletByUserId(user.getId()).get().getBalance();
                    LoginResponse loginRes = new LoginResponse(
                            user.getId().toString(), user.getUsername(), user.getFullName(),
                            user.getRole().name(), user.getEmail(), balance
                    );
                    return new Response(true, "Đăng nhập thành công!", loginRes);
                } else {
                    return new Response(false, "Sai mật khẩu!", null);
                }
            }
            return new Response(false, "Tài khoản không tồn tại!", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ cơ sở dữ liệu!", null);
        }
    }

    public Response handleDeposit(DepositRequest depositData) {
        try {
            Wallet wallet = userDao.findWalletByUserId(UUID.fromString(depositData.userId()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

            wallet.setBalance(wallet.getBalance().add(depositData.amount()));
            userDao.saveWallet(wallet);
            return new Response(true, "Nạp tiền thành công", null);
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Nạp thất bại", null);
        }
    }

    public Response handleRegister(RegisterRequest registerData) {
        try {
            // 1. Kiểm tra xem username đã tồn tại trong DB chưa?
            if (userDao.findByName(registerData.username()).isPresent()) {
                return new Response(false, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác!", null);
            } else {
                // 2. Tạo đối tượng User mới (Abstract factory ở UserFactory)
                Role roleEnum = Role.valueOf(registerData.role().toUpperCase());
                User newUser = UserFactory.create(roleEnum);

                // 3. Gắn dữ liệu
                newUser.setId(UUID.randomUUID());
                newUser.setUsername(registerData.username());
                newUser.setPasswordSalt(PasswordUtils.generateSalt());
                newUser.setPasswordHash(PasswordUtils.hashPassword(registerData.password(), newUser.getPasswordSalt()));
                newUser.setFullName(registerData.fullName());
                newUser.setEmail(registerData.email());
                newUser.setRole(roleEnum);
                newUser.setActive(true);
                newUser.setCreatedAt(now());
                newUser.setUpdatedAt(now());

                // 4. Gọi DAO để INSERT xuống database
                userDao.save(newUser);

                // 5. Tạo luôn một cái Ví (Wallet) 0 đồng cho tài khoản mới này
                Wallet newWallet = new Wallet(
                        UUID.randomUUID(),
                        now(),
                        now(),
                        newUser.getId(),
                        BigDecimal.ZERO
                );
                userDao.saveWallet(newWallet);

                // 6. Báo thành công
                return new Response(true, "Đăng ký tài khoản thành công!", null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi lưu dữ liệu!", null);
        }
    }

    public Response handleChangeInformation(ChangeInformationRequest changeInformationData) {
        String userId = changeInformationData.userId();
        String newFullName = changeInformationData.fullName();
        String newEmail = changeInformationData.email();

        try {
            // 1. Tìm User đó
            User user = userDao.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng này"));
            // 2. Thay đổi dữ liệu

            user.setFullName(newFullName);
            user.setEmail(newEmail);
            userDao.save(user);

            return new Response(true, "Thay đổi thành công", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi thay đổi thông tin", null);
        }
    }

    public Response handleChangePassword(ChangePasswordRequest changePasswordData) {
        String userId = changePasswordData.userId();
        String newPassword = changePasswordData.newPassword();

        try {
            User user = userDao.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng này"));

            user.setPasswordSalt(PasswordUtils.generateSalt());
            user.setPasswordHash(PasswordUtils.hashPassword(newPassword, user.getPasswordSalt()));

            userDao.save(user);

            return new Response(true, "Đổi mật khẩu thành công", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Lỗi máy chủ khi thay đổi mật khẩu", null);
        }
    }
}