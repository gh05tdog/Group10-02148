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
    private void initialize() {
        this.server = model.getServer();
    }

    @FXML
    private void handleSendAction() {
        String message = messageField.getText();
        try {
            server.put("message", chatroomField.getText(), usernameField.getText() + ": " + message + "\n"); // Send message
        } catch (InterruptedException e) {
            System.out.println("error:" + e);
        }
        //Clear the message field
        messageField.clear();
    }

    @FXML
    private void handleConnectAction(){
        try {
            // Check if a username is entered
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                System.out.println("Username is required to join the chat room.");
                return;
            }

            // Disconnect from the current room if already connected
            if (clientID != null && !clientID.isEmpty()) {
                server.put("leave", chatroomField.getText(), clientID);
                terminateThreads(); // Terminate existing threads
                Platform.runLater(() -> usernameList.clear()); // Clear the username list
            }

            // Update the clientID with the provided username
            clientID = username;

            // Get the room ID from the chatroomField
            String roomID = chatroomField.getText().trim();
            if (roomID.isEmpty()) {
                System.out.println("Room ID is required to join the chat room.");
                return;
            }

            // Ensure server connection is established
            if (server == null) {
                System.out.println("Server connection is not established.");
                model.setServer(config.getIp());
                server = model.getServer();
            }

            // Join the new chat room
            server.put("join", roomID, clientID);

            // Start threads for handling messages and user updates
            startMessageThread(roomID, clientID);
            startUserThread(roomID, clientID);

        } catch (InterruptedException | IOException e) {
            System.out.println("Error in handleConnectAction: " + e.getMessage());
        }
    }

    private void terminateThreads() {
        if (messageThread != null && messageThread.isAlive()) {
            messageThread.interrupt();
        }
        if (userThread != null && userThread.isAlive()) {
            userThread.interrupt();
        }
    }



    private void startMessageThread(String roomID, String clientID) {
        messageThread = new Thread(() -> {
            try {
                while (true) {
                    Object[] response = server.get(new ActualField("message"), new ActualField(roomID), new ActualField(clientID), new FormalField(String.class));
                    messageArea.appendText((String) response[3]);
                }
            } catch (Exception e) {
                System.out.println("Messagethread error: " + e);
            }
        });
        messageThread.start();
    }

    private void startUserThread(String roomID, String clientID) {
        userThread = new Thread(() -> {
            try {
                while (true) {
                    Object[] response = server.get(new ActualField("users"), new ActualField(roomID), new ActualField(clientID), new FormalField(String.class));
                    String userList = (String) response[3];
                    Platform.runLater(() -> usernameList.setText(userList));
                }
            } catch (Exception e) {
                System.out.println("User thread: " + e);
            }
        });
        userThread.start();
    }
}
