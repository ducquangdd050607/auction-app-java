package com.auctionapp.auctionappjava.server.network;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Bắt đầu phục vụ Client: " + socket.getInetAddress());

        try {
            // TODO Bước 1: Sau khi có Request/Response, mở comment 3 dòng dưới đây
            // out = new ObjectOutputStream(socket.getOutputStream());
            // out.flush();
            // in = new ObjectInputStream(socket.getInputStream());

            System.out.println("[" + threadName + "] Đã kết nối thành công. Đang chờ các chức năng DAO hoàn thiện...");

            // TODO Bước 2: Tạo vòng lặp while(true) ở đây để đọc Request

            // TẠM THỜI: Giữ cho luồng này sống để Socket không bị đóng ngay lập tức
            // Vòng lặp rỗng này giúp Server không bị treo, mô phỏng việc đang "trò chuyện"
            while (!socket.isClosed()) {
                Thread.sleep(1000); // Ngủ 1 giây để không ăn CPU của máy tính
            }

        } catch (Exception e) {
            System.out.println("[" + threadName + "] Client đã ngắt kết nối. Lỗi: " + e.getMessage());
        } finally {
            // TODO Bước 3: Đóng in/out stream ở đây
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("[" + threadName + "] Trở về trạng thái rảnh rỗi chờ việc mới.");
        }
    }
}