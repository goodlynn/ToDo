package com.example.lab2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ResourceBundle;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("com.example.lab2.messages");
        FXMLLoader fxmlLoader = new FXMLLoader(
                HelloApplication.class.getResource("todo-view.fxml"),
                bundle
        );
        Scene scene = new Scene(fxmlLoader.load(), 980, 640);
        scene.getStylesheets().add(HelloApplication.class.getResource("todo.css").toExternalForm());
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setTitle(bundle.getString("app.title"));
        stage.setScene(scene);
        stage.show();
    }
}
