package com.auctionapp.auctionappjava.client.network;
<<<<<<< HEAD

import com.auctionapp.auctionappjava.common.dto.AuctionRealtimeEvent;
=======
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
import com.auctionapp.auctionappjava.common.dto.Request;
import com.auctionapp.auctionappjava.common.dto.Response;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
<<<<<<< HEAD
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
=======
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938

public class Client {
    private static Client instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
<<<<<<< HEAD
    private Thread listenerThread;
    private volatile boolean running;

    private final Object writeLock = new Object();
    private final LinkedBlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>();
    private final List<Consumer<AuctionRealtimeEvent>> realtimeListeners = new CopyOnWriteArrayList<>();

=======

    // Singleton Pattern: Đảm bảo chỉ có 1 NetworkClient tồn tại
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
    private Client() {}

    public static synchronized Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

<<<<<<< HEAD
    public void connect(String serverIp, int port) throws Exception {
        socket = new Socket(serverIp, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        running = true;
        startListenerThread();
        System.out.println("Đã kết nối thành công tới Server!");
    }

    public Response sendRequest(Request request) throws Exception {
=======
    // Hàm gọi đến khi app vừa khởi động
    public void connect(String serverIp, int port) throws Exception {
        socket = new Socket(serverIp, port);

        // Output luôn khởi tạo trước Input
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
        System.out.println("Đã kết nối thành công tới Server!");
    }

    // Hàm dùng chung cho mọi Controller để gửi Request và lấy Response
    public synchronized Response sendRequest(Request request) throws Exception {
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
        if (socket == null || socket.isClosed()) {
            throw new Exception("Chưa kết nối đến máy chủ!");
        }

<<<<<<< HEAD
        synchronized (writeLock) {
            out.writeObject(request);
            out.flush();
            out.reset();
        }

        return responseQueue.take();
    }

    public void addRealtimeListener(Consumer<AuctionRealtimeEvent> listener) {
        if (listener != null) {
            realtimeListeners.add(listener);
        }
    }

    public void removeRealtimeListener(Consumer<AuctionRealtimeEvent> listener) {
        realtimeListeners.remove(listener);
    }

    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            while (running) {
                try {
                    Object object = in.readObject();
                    if (object instanceof Response response) {
                        responseQueue.offer(response);
                    } else if (object instanceof AuctionRealtimeEvent event) {
                        for (Consumer<AuctionRealtimeEvent> listener : realtimeListeners) {
                            listener.accept(event);
                        }
                    }
                } catch (Exception e) {
                    if (running) {
                        responseQueue.offer(new Response(false, "Mất kết nối server: " + e.getMessage(), null));
                    }
                    running = false;
                }
            }
        }, "auction-client-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void disconnect() {
        running = false;
=======
        // Ném gói tin lên Server
        out.writeObject(request);
        out.flush();

        // Đứng đợi và hứng kết quả Server trả về
        return (Response) in.readObject();
    }

    // Gọi khi người dùng tắt App
    public void disconnect() {
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
