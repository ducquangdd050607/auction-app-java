package com.auctionapp.auctionappjava.client.network;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    private static Client instance;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Singleton Pattern: Đảm bảo chỉ có 1 NetworkClient tồn tại
    private Client() {}

    public static synchronized Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    // Hàm gọi đến khi app vừa khởi động
    public void connect(String serverIp, int port) throws Exception {
        socket = new Socket(serverIp, port);

        // TODO: nào mở cmt bên clienthandler thì mở ở đây
        // Output luôn khởi tạo trước Input
        /*out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());*/
        System.out.println("Đã kết nối thành công tới Server!");
    }

    // TODO: Phần dùng để gửi request sau khi có DAO
    // Hàm dùng chung cho mọi Controller để gửi Request và lấy Response
    /*public synchronized Response sendRequest(Request request) throws Exception {
        if (socket == null || socket.isClosed()) {
            throw new Exception("Chưa kết nối đến máy chủ!");
        }

        // Ném gói tin lên Server
        out.writeObject(request);
        out.flush();

        // Đứng đợi và hứng kết quả Server trả về
        return (Response) in.readObject();
    }*/

    // Gọi khi người dùng tắt App
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}