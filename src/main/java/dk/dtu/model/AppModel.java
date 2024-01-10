package dk.dtu.model;

import dk.dtu.config;
import dk.dtu.controller.LobbyController;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.jspace.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    public void sendMessage(String clientID, String message, String roomId) throws InterruptedException {
        server.put("message", clientID, message, roomId);
        System.out.println("Sent message: " + message);
    }

    public void joinLobby(String clientID) throws InterruptedException {
        server.put("joinLobby", clientID);
    }

    public void leaveLobby(String clientID) throws InterruptedException {
        server.put("leaveLobby", clientID);
    }

    public void startListeningForMessages(TextArea messageAreaLobby) {
        messageThread = new Thread(() -> {
            try {
                List<String> lastSeenMessages = new ArrayList<>();
                while (true) {
                    Object[] response = server.query(new ActualField("messages"), new FormalField(List.class));
                    List<String> newMessages = (List<String>) response[1];

                    if (!newMessages.equals(lastSeenMessages)) {
                        Platform.runLater(() -> {

                            messageAreaLobby.clear();
                            for (String msg : newMessages) {
                                messageAreaLobby.appendText(msg + "\n");
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
    public void startListeningForDayNightCycle(LobbyController lobbyController) {
        new Thread(() -> {
            try {
                while (true) {
                    // Listen for any type of message

                    Object[] response = server.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));

                    String messageType = (String) response[0];
                    String messageContent = (String) response[2];

                    if ("dayNightCycle".equals(messageType)) {
                        Platform.runLater(() -> lobbyController.updateDayNightCycle(messageContent));
                    } else if ("timeUpdate".equals(messageType)) {
                        Platform.runLater(() -> lobbyController.updateTimeLabel(messageContent));
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Updates listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in updates listening thread: " + e);
            }
        }).start();
    }

    public void startGame() throws InterruptedException {
        server.put("startGame", "some_identifier_or_info");
        System.out.println("Requested to start game");
    }

    public void startListeningForUserUpdates(TextArea userListArea, String clientID) {
        userThread = new Thread(() -> {
            try {
                while (true) {
                    // Retrieve the user list update intended for this client
                    Object[] response = server.get(new ActualField("userUpdate"), new ActualField(clientID), new FormalField(String.class));
                    System.out.println("Received user list update: " + Arrays.toString(response));
                    if (response != null) {
                        String currentUserList = (String) response[2];
                        System.out.println("Received user list update: " + currentUserList);
                        Platform.runLater(() -> userListArea.setText(currentUserList));
                    }

                    // Sleep to prevent excessive querying
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("User update listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in user update listening thread: " + e);
            }
        });
        userThread.start();
    }




}
