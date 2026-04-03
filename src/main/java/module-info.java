module com.auctionapp.auctionappjava {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.auctionapp.auctionappjava.controllers to javafx.fxml;
    exports com.auctionapp.auctionappjava;
}