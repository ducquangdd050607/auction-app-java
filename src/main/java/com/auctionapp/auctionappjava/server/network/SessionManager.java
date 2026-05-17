package com.auctionapp.auctionappjava.server.network;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static SessionManager instance;
    // Map lưu UserId -> Luồng xuất dữ liệu của họ
    private final ConcurrentHashMap<String, ObjectOutputStream> activeSessions = new ConcurrentHashMap<>();

    // Singleton
    private SessionManager() {}
    public static synchronized SessionManager getInstance() {
        if (instance == null) instance = new SessionManager();
        return instance;
    }

    public void registerSession(String userId, ObjectOutputStream out) {
        activeSessions.put(userId, out);
    }

    public void removeSession(String userId) {
        activeSessions.remove(userId);
    }

    // Gửi đích danh cho 1 người
    public void sendToUser(String userId, Object message) {
        ObjectOutputStream out = activeSessions.get(userId);
        if (out != null) {
            try {
                out.writeObject(message);
                out.flush();
            } catch (Exception e) {
                removeSession(userId);
            }
        }
    }

    // Gửi cho tất cả mọi người
    public void broadcast(Object message) {
        activeSessions.forEach((id, out) -> sendToUser(id, message));
    }
}