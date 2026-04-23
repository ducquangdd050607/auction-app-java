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

    exports com.auctionapp.auctionappjava;
    exports com.auctionapp.auctionappjava.client;
    exports com.auctionapp.auctionappjava.client.config;
    exports com.auctionapp.auctionappjava.client.core;
    exports com.auctionapp.auctionappjava.client.network;
    exports com.auctionapp.auctionappjava.client.service;
    exports com.auctionapp.auctionappjava.client.session;
    exports com.auctionapp.auctionappjava.common.config;
    exports com.auctionapp.auctionappjava.common.dto;
    exports com.auctionapp.auctionappjava.common.enums;
    exports com.auctionapp.auctionappjava.common.exception;
    exports com.auctionapp.auctionappjava.common.factory;
    exports com.auctionapp.auctionappjava.common.model;
    exports com.auctionapp.auctionappjava.common.observer;
    exports com.auctionapp.auctionappjava.common.strategy;
    exports com.auctionapp.auctionappjava.common.util;
    exports com.auctionapp.auctionappjava.server;
    exports com.auctionapp.auctionappjava.server.config;
    exports com.auctionapp.auctionappjava.server.dao;
    exports com.auctionapp.auctionappjava.server.dao.jdbc;
    exports com.auctionapp.auctionappjava.server.db;
    exports com.auctionapp.auctionappjava.server.network;
    exports com.auctionapp.auctionappjava.server.scheduler;
    exports com.auctionapp.auctionappjava.server.service;
}
