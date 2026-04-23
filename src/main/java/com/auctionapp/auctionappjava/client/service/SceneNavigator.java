package com.auctionapp.auctionappjava.client.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneNavigator {
    public Parent load(String resource) throws IOException { return FXMLLoader.load(SceneNavigator.class.getResource(resource)); }
    public void showWindow(String resource, String title) throws IOException { Stage stage=new Stage(); stage.setTitle(title); stage.setScene(new Scene(load(resource))); stage.show(); }
    public void showModal(Stage owner, String resource, String title) throws IOException { Stage stage=new Stage(); stage.setTitle(title); stage.initOwner(owner); stage.initModality(Modality.WINDOW_MODAL); stage.setScene(new Scene(load(resource))); stage.showAndWait(); }
}
