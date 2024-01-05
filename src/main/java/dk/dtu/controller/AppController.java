package dk.dtu.controller;

import dk.dtu.model.AppModel;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AppController {
    @FXML
    private TextField messageField;
    @FXML
    private TextArea messageArea;

    private final AppModel model;

    public AppController() {
        model = new AppModel();
    }

    @FXML
    private void initialize() {
        // You might want to start a new thread to listen for messages
        // This is just a placeholder for the actual message receiving logic
    }

    @FXML
    private void handleSendAction() {
        String message = messageField.getText();
        try {
            model.sendMessage(message);
            messageField.clear();
            String getMess = model.receiveMessage();
            messageArea.appendText("Sent: " + getMess + "\n");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
