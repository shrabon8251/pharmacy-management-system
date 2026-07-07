package com.example.pharmacy_management_system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PharmacyManagement extends Application {

    public static Stage stage;
    public static String currentUser;

    @Override
    public void start(Stage stage) throws IOException {
        PharmacyManagement.stage = stage;

        FXMLLoader fxmlLoader = new FXMLLoader(PharmacyManagement.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        stage.setTitle("Pharmacy Management System");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void sceneChange(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(PharmacyManagement.class.getResource(fxml + ".fxml"));
        double width = stage.getScene() != null ? stage.getScene().getWidth() : 1280;
        double height = stage.getScene() != null ? stage.getScene().getHeight() : 720;
        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setTitle("Pharmacy Management System");
        stage.setScene(scene);
        stage.show();
    }
}
