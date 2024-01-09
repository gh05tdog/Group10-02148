package dk.dtu.controller;

import dk.dtu.model.AppModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.IOException;

public class AppController {
    public TextArea usernameList;
    public TextField usernameField;
    @FXML
    private TextField messageField;
    @FXML
    private TextArea messageArea;

    private final AppModel model;
    private String clientID;

    public AppController() throws IOException {
        model = new AppModel();
    }

    @FXML
    private void handleSendAction() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                System.out.println("Sending message: " + message);
                model.sendMessage(clientID, message);
            } catch (Exception e) {
                System.out.println("Error sending message: " + e);
            }
            messageField.clear();
        }
    }

    @FXML
    private void handleConnectAction() throws InterruptedException {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            System.out.println("Username is required to join the lobby.");
            return;
        }
        if (clientID != null && !clientID.isEmpty()) {
            model.leaveLobby(clientID);
        }
        clientID = username;
        model.joinLobby(clientID);
        model.startListeningForMessages(messageArea, clientID);
        model.startListeningForUserUpdates(usernameList, clientID);
    }
}
