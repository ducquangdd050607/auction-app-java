package com.auctionapp.auctionappjava.server.network;

import com.auctionapp.auctionappjava.common.dto.*;
<<<<<<< HEAD
import com.auctionapp.auctionappjava.server.realtime.AuctionRealtimeHub;
import com.auctionapp.auctionappjava.server.realtime.ClientConnection;
import com.auctionapp.auctionappjava.server.service.AuctionService;
import com.auctionapp.auctionappjava.server.service.AutoBidService;
import com.auctionapp.auctionappjava.server.service.BidHistoryService;
=======
import com.auctionapp.auctionappjava.server.service.AuctionService;
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
import com.auctionapp.auctionappjava.server.service.UserService;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
<<<<<<< HEAD
import java.util.UUID;

public class ClientHandler implements Runnable {
    private final Socket socket;

    private final UserService userService = new UserService();
    private final AuctionService auctionService = new AuctionService();
    private final AutoBidService autoBidService = new AutoBidService();
    private final BidHistoryService bidHistoryService = new BidHistoryService();
    private final AuctionRealtimeHub realtimeHub = AuctionRealtimeHub.getInstance();
=======

public class ClientHandler implements Runnable {
    private Socket socket;

    // Khởi tạo các Service
    private final UserService userService = new UserService();
    private final AuctionService auctionService = new AuctionService();
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.println("[" + threadName + "] Bắt đầu phục vụ Client: " + socket.getInetAddress());
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
<<<<<<< HEAD
        ClientConnection connection = null;
=======
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
<<<<<<< HEAD
            connection = new ClientConnection(out);
=======
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Request request = (Request) in.readObject();
<<<<<<< HEAD
                Response response;

                switch (request.action()) {
=======
                Response response = null;

                // Sử dụng Switch-case hiện đại của Java để định tuyến (Routing)
                switch (request.action()) {
                    // Case thuộc user
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
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

<<<<<<< HEAD
=======
                    // Case thuộc auction
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
                    case "GET_ALL_AUCTIONS":
                        response = auctionService.handleGetAllAuctions();
                        break;
                    case "GET_ALL_UPLOADED_AUCTIONS":
                        response = auctionService.handleGetAllUploadedAuctions((ManagerAndHistoryRequest) request.payload());
                        break;
                    case "GET_ALL_FEATURED_AUCTIONS":
                        response = auctionService.handleGetAllFeaturedAuctions();
                        break;
                    case "GET_HISTORY":
                        response = auctionService.handleGetAllPersonalBiddedAuctions((ManagerAndHistoryRequest) request.payload());
                        break;
                    case "GET_TRANSACTIONS":
                        response = auctionService.handleGetAllBiddedAuctions((ManagerAndHistoryRequest) request.payload());
                        break;
                    case "ADD_ITEM":
                        response = auctionService.handleAddItem((AddItemRequest) request.payload());
                        break;
                    case "PLACE_BID":
                        response = auctionService.handlePlaceBid((PlaceBidRequest) request.payload());
                        break;
<<<<<<< HEAD
                    case "CONFIGURE_AUTO_BID":
                        response = autoBidService.handleConfigureAutoBid((ConfigureAutoBidRequest) request.payload());
                        break;
                    case "GET_BID_HISTORY":
                        response = bidHistoryService.handleGetBidHistory(request.payload());
                        break;
                    case "SUBSCRIBE_AUCTION":
                        realtimeHub.subscribe(parseAuctionId(request.payload()), connection);
                        response = new Response(true, "Đã subscribe realtime auction.", null);
                        break;
                    case "UNSUBSCRIBE_AUCTION":
                        realtimeHub.unsubscribe(parseAuctionId(request.payload()), connection);
                        response = new Response(true, "Đã unsubscribe realtime auction.", null);
                        break;
=======
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
                    case "GET_USERS":
                        response = auctionService.handleGetUsers();
                        break;
                    case "REMOVE_AUCTION":
                        response = auctionService.handleRemoveAuction((RemoveAuctionRequest) request.payload());
                        break;
                    case "DECIDE_STATUS":
                        response = auctionService.handleSetUserStatus((ManagerAndHistoryRequest) request.payload());
                        break;
                    default:
                        response = new Response(false, "Hành động không hợp lệ: " + request.action(), null);
                        break;
                }

<<<<<<< HEAD
                connection.sendResponse(response);
=======
                // Gửi kết quả duy nhất 1 lần ở đây
                if (response != null) {
                    out.writeObject(response);
                    out.flush();
                }
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
            }

        } catch (Exception e) {
            System.out.println("[" + threadName + "] Client đã ngắt kết nối. Lỗi: " + e.getMessage());
        } finally {
<<<<<<< HEAD
            if (connection != null) {
                realtimeHub.unsubscribeAll(connection);
            }
=======
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
<<<<<<< HEAD

    private UUID parseAuctionId(Object payload) {
        if (payload instanceof UUID uuid) return uuid;
        if (payload instanceof String text) return UUID.fromString(text);
        throw new IllegalArgumentException("auctionId không hợp lệ");
    }
}
=======
}
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
