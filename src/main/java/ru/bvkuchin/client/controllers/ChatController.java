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
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import ru.bvkuchin.client.IONet;

public class ChatController {


    public ListView<String> listView;
    public TextField textField;
    public Button sendButton;
    public Button refreshButton;
    public MenuItem quitButton;
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
        String item = listView.getSelectionModel().getSelectedItem();
        File file = new File(item);
        net.sendMsg("/filename>>" + file.getName());
        net.sendFile(file);
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

    public void quitAction(ActionEvent actionEvent) throws IOException {
     net.close();
        System.exit(0);

    }
}




