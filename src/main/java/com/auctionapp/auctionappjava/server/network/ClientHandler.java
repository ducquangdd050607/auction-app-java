package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.client.session.UserSession;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.server.service.AuctionService;
import com.auctionapp.auctionappjava.server.service.UserService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private Socket socket;

    // Khởi tạo các Service
    private final UserService userService = new UserService();
    private final AuctionService auctionService = new AuctionService();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Bắt đầu phục vụ Client: " + socket.getInetAddress());
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Request request = (Request) in.readObject();
                Response response = null;

                // Sử dụng Switch-case hiện đại của Java để định tuyến (Routing)
                switch (request.action()) {
                    // Case thuộc user
                    case "LOGIN":
                        response = userService.handleLogin((LoginRequest) request.payload());
                        break;
                    case "REGISTER":
                        response = userService.handleRegister((RegisterRequest) request.payload());
                        break;
                    case "DEPOSIT":
                        response = userService.handleDeposit((DepositRequest) request.payload());
                        break;
                    case "CHANGE_INFORMATION":
                        response = userService.handleChangeInformation((ChangeInformationRequest) request.payload());
                        break;
                    case "CHANGE_PASSWORD":
                        response = userService.handleChangePassword((ChangePasswordRequest) request.payload());
                        break;

                    // Case thuộc auction
                    case "GET_ALL_AUCTIONS":
                        response = auctionService.handleGetAllAuctions();
                        break;
                    case "GET_ALL_UPLOADED_AUCTIONS":
                        response = auctionService.handleGetAllUploadedAuctions((BidManagerRequest) request.payload());
                        break;
                    case "ADD_ITEM":
                        response = auctionService.handleAddItem((AddItemRequest) request.payload());
                        break;
                    case "PLACE_BID":
                        response = auctionService.handlePlaceBid((PlaceBidRequest) request.payload());
                        break;

                    default:
                        response = new Response(false, "Hành động không hợp lệ: " + request.action(), null);
                        break;
                }

                // Gửi kết quả duy nhất 1 lần ở đây
                if (response != null) {
                    out.writeObject(response);
                    out.flush();
                }
            }

        } catch (Exception e) {
            System.out.println("[" + threadName + "] Client đã ngắt kết nối. Lỗi: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}