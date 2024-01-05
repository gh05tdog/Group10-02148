package dk.dtu.controller;

import dk.dtu.model.AppModel;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.UUID;

public class AppController {
    public TextField usernameField;
    public TextField chatroomField;
    @FXML
    private TextField messageField;
    @FXML
    private TextArea messageArea;

    private final AppModel model;

    private RemoteSpace server;

    public AppController() throws IOException {
        model = new AppModel();
        this.server = model.getServer();
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
            server.put("message", chatroomField.getText(), usernameField.getText() + ": " + message + "\n"); // Send message
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }

    @FXML
    private void handleConnectAction(){
        try {
            String roomID = chatroomField.getText();
            String clientID = UUID.randomUUID().toString(); // Unique ID for each client


            // Join the chat room
            server.put("join", roomID, clientID);

            new Thread(() -> {
                try {
                    while (true) {
                        Object[] response = server.get(new ActualField("message"), new ActualField(roomID), new ActualField(clientID), new FormalField(String.class));
                        messageArea.appendText((String) response[3]);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }).start();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}
