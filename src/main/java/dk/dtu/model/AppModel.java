package dk.dtu.model;

import dk.dtu.config;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.jspace.*;

import java.io.IOException;


public class AppModel {
    private RemoteSpace server;
    private Thread messageThread;
    private Thread userThread;

    public AppModel() throws IOException {
        this.server = new RemoteSpace(config.getIp() + "/chat?keep");
    }

    public void setServer(String ip) throws IOException {
        this.server = new RemoteSpace(ip + "/game?keep");
    }

    public void sendMessage(String clientID, String message) throws InterruptedException {
        server.put("message", clientID, message); // Ensure these keys align with server's expectations
        System.out.println("Sent message: " + message);
    }


    public void joinLobby(String clientID) throws InterruptedException {
        server.put("joinLobby", clientID);
    }

    public void leaveLobby(String clientID) throws InterruptedException {
        server.put("leaveLobby", clientID);
    }

    public void startListeningForMessages(TextArea messageArea, String clientID) {
        // Implement message listening logic

        messageThread = new Thread(() -> {
            try {
                while (true) {
                    Object[] response = server.get(new ActualField("message"), new ActualField(clientID), new FormalField(String.class));
                    messageArea.appendText((String) response[2]);
                }
            } catch (Exception e) {
                System.out.println("Message thread error: " + e);
            }
        });
        messageThread.start();
    }

    public void startListeningForUserUpdates(TextArea usernameList, String clientID) {
        userThread = new Thread(() -> {
            try {
                while (true) {
                    Object[] response = server.get(new ActualField("users"), new ActualField(clientID), new FormalField(String.class));
                    String userList = (String) response[2];
                    Platform.runLater(() -> usernameList.setText(userList));
                }
            } catch (Exception e) {
                System.out.println("User thread error: " + e);
            }
        });
        userThread.start();
    }

}
