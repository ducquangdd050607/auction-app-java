module com.auctionapp.auctionappjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;


    opens com.auctionapp.auctionappjava.client.controllers to javafx.fxml;
    opens com.auctionapp.auctionappjava.common.util to javafx.fxml;

    exports com.auctionapp.auctionappjava.client;
    exports com.auctionapp.auctionappjava.server;
    exports com.auctionapp.auctionappjava.server.model;

}