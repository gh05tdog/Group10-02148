package dk.dtu.model;

import dk.dtu.config;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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

    public void startListeningForMessages(TextArea messageArea) {
        messageThread = new Thread(() -> {
            try {
                List<String> lastSeenMessages = new ArrayList<>();
                while (true) {
                    Object[] response = server.get(new ActualField("messages"), new FormalField(List.class));
                    List<String> newMessages = (List<String>) response[1];

                    if (!newMessages.equals(lastSeenMessages)) {
                        Platform.runLater(() -> {
                            messageArea.clear();
                            for (String msg : newMessages) {
                                messageArea.appendText(msg + "\n");
                            }
                        });
                        lastSeenMessages = new ArrayList<>(newMessages); // Update last seen messages
                    }

                    // Sleep for a short duration before checking for new messages again
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("Message listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in message listening thread: " + e);
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
