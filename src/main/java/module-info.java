module com.auctionapp.auctionappjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;

    opens com.auctionapp.auctionappjava.client.controllers to javafx.fxml;

    exports com.auctionapp.auctionappjava.client;
    exports com.auctionapp.auctionappjava.server;
    exports com.auctionapp.auctionappjava.common.model;
}