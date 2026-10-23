package com.example.dearfreshman;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                HelloApplication.class.getResource("hello-view.fxml")
        );

        Scene scene = new Scene(loader.load(), 1100, 720);

        stage.setTitle("Dear Freshman | A College Memory");
        stage.setScene(scene);
        stage.setMinWidth(850);
        stage.setMinHeight(650);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
