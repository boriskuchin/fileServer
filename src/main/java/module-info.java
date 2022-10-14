module ru.bvkuchin.client {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.bvkuchin.client to javafx.fxml;
    exports ru.bvkuchin.client;
    exports ru.bvkuchin.client.controllers;
    opens ru.bvkuchin.client.controllers to javafx.fxml;
}