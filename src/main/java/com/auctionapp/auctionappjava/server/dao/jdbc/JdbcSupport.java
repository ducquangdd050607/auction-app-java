package com.auctionapp.auctionappjava.server.dao.jdbc;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

final class JdbcSupport {
    private JdbcSupport() {
    }

    static String uuid(UUID id) {
        return id == null ? null : id.toString();
    }

    static UUID uuid(String s) {
        return s == null ? null : UUID.fromString(s);
    }

    static Timestamp ts(LocalDateTime t) {
        return t == null ? null : Timestamp.valueOf(t);
    }

    static LocalDateTime ldt(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }

    static BigDecimal money(ResultSet rs, String c) throws SQLException {
        BigDecimal v = rs.getBigDecimal(c);
        return v == null ? BigDecimal.ZERO : v;
    }
}
