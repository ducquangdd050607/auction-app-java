package com.auctionapp.auctionappjava.server.db;

import com.auctionapp.auctionappjava.common.enums.Role;
import com.auctionapp.auctionappjava.common.util.PasswordUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.UUID;

public class DatabaseInitializer {
    private final DatabaseManager databaseManager;

    public DatabaseInitializer(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void initialize() {
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream("/db/schema.sql")) {
            if (in == null) throw new IllegalStateException("Không tìm thấy /db/schema.sql");
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("(?s)/\\*.*?\\*/", "")
                    .replaceAll("(?m)^\\s*#.*$", "")
                    .replaceAll("(?m)^\\s*--.*$", "");
            try (Connection c = databaseManager.getConnection(); Statement st = c.createStatement()) {
                for (String cmd : sql.split(";")) {
                    if (!cmd.trim().isEmpty()) st.execute(cmd.trim());
                }
            }
            seedAdmin();
        } catch (Exception e) {
            throw new IllegalStateException("Không thể khởi tạo database", e);
        }
    }

    private void seedAdmin() throws Exception {
        try (Connection c = databaseManager.getConnection(); PreparedStatement count = c.prepareStatement("SELECT COUNT(*) FROM users WHERE role=?")) {
            count.setString(1, Role.ADMIN.name());
            try (ResultSet rs = count.executeQuery()) {
                if (rs.next() && rs.getLong(1) > 0) return;
            }
            String salt = PasswordUtils.generateSalt();
            String hash = PasswordUtils.hashPassword("admin123", salt);
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO users (id, username, password_hash, password_salt, full_name, email, role, active, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?)")) {
                LocalDateTime now = LocalDateTime.now();
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, "admin");
                ps.setString(3, hash);
                ps.setString(4, salt);
                ps.setString(5, "System Administrator");
                ps.setString(6, "admin@auction.local");
                ps.setString(7, Role.ADMIN.name());
                ps.setBoolean(8, true);
                ps.setObject(9, now);
                ps.setObject(10, now);
                ps.executeUpdate();
            }
        }
    }
}
