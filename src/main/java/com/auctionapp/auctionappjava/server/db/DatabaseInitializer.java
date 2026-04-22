package com.auctionapp.auctionappjava.server.db;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class DatabaseInitializer {
    private final DatabaseManager db;

    public DatabaseInitializer(DatabaseManager db) {
        this.db = db;
    }

    public void runSchema() {
        executeResource("db/mysql-schema.sql");
    }

    public void runSeed() {
        executeResource("db/schema.sql");
    }

    public void executeResource(String path) {
        String sql = read(path);
        if (sql.isBlank()) return;
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            for (String s : split(sql)) {
                if (!s.trim().isBlank()) st.execute(s.trim());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot initialize database " + path, e);
        }
    }

    private String read(String path) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(path)) {
            if (in == null) return "";
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            StringBuilder b = new StringBuilder();
            for (String line; (line = br.readLine()) != null; ) {
                String t = line.trim();
                if (!t.startsWith("--") && !t.startsWith("#")) b.append(line).append('\n');
            }
            return b.toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<String> split(String sql) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean q = false;
        for (int i = 0; i < sql.length(); i++) {
            char ch = sql.charAt(i);
            if (ch == '\'' && (i == 0 || sql.charAt(i - 1) != '\\')) q = !q;
            if (ch == ';' && !q) {
                out.add(cur.toString());
                cur.setLength(0);
            } else cur.append(ch);
        }
        if (!cur.isEmpty()) out.add(cur.toString());
        return out;
    }
}
