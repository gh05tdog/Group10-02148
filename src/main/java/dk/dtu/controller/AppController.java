package dk.dtu.controller;

import dk.dtu.config;
import dk.dtu.model.AppModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.UUID;

public class AppController {
    public TextArea usernameList;
    private RemoteSpace server;
    public TextField usernameField;
    public TextField chatroomField;
    @FXML
    private TextField messageField;
    @FXML
    private TextArea messageArea;

    private final AppModel model;

    private Thread messageThread;
    private Thread userThread;
    private String clientID;


    public AppController() throws IOException {
        model = new AppModel();
        this.server = model.getServer();
    }

    @FXML
    private void initialize() throws IOException {
        this.server = model.getServer();
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
            // Disconnect from the current room
            if (clientID != null) {
                server.put("leave", chatroomField.getText(), clientID);
                if (messageThread != null && messageThread.isAlive()) {
                    messageThread.interrupt();
                }
                if (userThread != null && userThread.isAlive()) {
                    userThread.interrupt();
                }
                // Clear the username list
                Platform.runLater(() -> usernameList.clear());
            }

            // Connect to the new room
            String roomID = chatroomField.getText();
            String clientID = UUID.randomUUID().toString();

            if (server == null) {
                System.out.println("Server is null");
                System.out.println("Server is now: " + config.getIp());
                model.setServer(config.getIp());
                server = model.getServer();
                System.out.println("Server is now: " + server.toString());
            }

            // Join the chat room
            server.put("join", roomID, clientID);

            new Thread(() -> {
                try {
                    System.out.println("Getting messages");
                    while (true) {
                        Object[] response = server.get(new ActualField("message"), new ActualField(roomID), new ActualField(clientID), new FormalField(String.class));
                        messageArea.appendText((String) response[3]);
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
            }).start();

            new Thread(() -> {
                try {
                    System.out.println("Getting users");
                    while (true) {
                        Object[] response = server.get(new ActualField("users"), new ActualField(roomID), new ActualField(clientID), new FormalField(String.class));
                        Platform.runLater(() -> {
                            usernameList.clear();
                            usernameList.appendText((String) response[3]);
                        });
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e);
                }
            }).start();

        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
