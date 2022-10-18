package ru.bvkuchin.client;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;


public class ChatApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent parent = FXMLLoader.load(getClass().getResource("clientView.fxml"));
        Scene scene = new Scene(parent);

        stage.setTitle("Chat");
        stage.setScene(scene);
        stage.show();

    }
}
