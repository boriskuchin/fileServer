package ru.bvkuchin.client.controllers;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.bvkuchin.client.IONet;

public class ChatController {


    public ListView<String> listView;
    public TextField textField;
    public Button sendButton;
    public Button refreshButton;
    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;
    private IONet net;

    @FXML
    void initialize() {
        try {
            Socket socket = new Socket("localhost", 9999);
            net = new IONet(socket, this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addTextToList(String message) {
        Platform.runLater(() -> listView.getItems().add(message));
    }

    public void sendText(ActionEvent actionEvent) throws IOException {
        net.sendMsg(textField.getText());
        textField.setText("");

    }

    public void refreshList(ActionEvent actionEvent) {
        String pathString = textField.getText();
        File folder = new File(pathString);
        if (folder.isDirectory()) {
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    addTextToList(fileEntry.getAbsolutePath());
                }
            }
        }

    }
}




