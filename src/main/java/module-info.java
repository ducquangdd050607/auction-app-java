module com.auctionapp.auctionappjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;

<<<<<<< HEAD
=======

>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
    opens com.auctionapp.auctionappjava.client.controllers to javafx.fxml;
    opens com.auctionapp.auctionappjava.common.util to javafx.fxml;

    exports com.auctionapp.auctionappjava.client;
<<<<<<< HEAD
    exports com.auctionapp.auctionappjava.client.network;
    exports com.auctionapp.auctionappjava.server;
    exports com.auctionapp.auctionappjava.common.dto;
    exports com.auctionapp.auctionappjava.common.enums;
    exports com.auctionapp.auctionappjava.common.model;
}
=======
    exports com.auctionapp.auctionappjava.server;
    exports com.auctionapp.auctionappjava.common.model;

}
>>>>>>> 48bf0f83663782457a4ff6c1ac69291ad16fd938
