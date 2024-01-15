package dk.dtu.model;

import dk.dtu.config;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.Objects;

public class LobbyManager {
    private final RemoteSpace server;

    public LobbyManager(RemoteSpace server) {
        this.server = server;
    }

    public void joinLobby(String username) throws InterruptedException {
        server.put("joinLobby", username);
        System.out.println("User " + username + " joined the lobby");
        if(!config.getLobbyLeader()){
        server.put("CheckUsernameLock");
        }
    }

    public void startGame() throws InterruptedException {
        server.put("startGame", "game_identifier");
        System.out.println("Game is starting");
    }

    public void startListeningForUserUpdates(TextArea userListArea, String clientID) {
        Thread userThread = new Thread(() -> {
            try {
                while (true) {
                    // Retrieve the user list update intended for this client
                    Object[] response = server.get(new ActualField("userUpdate"), new ActualField(clientID), new FormalField(String.class));
                    if (response != null) {
                        // Update the user list
                        String currentUserList = (String) response[2];
                        String finalCurrentUserList = currentUserList.replace(", ", "\n");
                        Platform.runLater(() -> userListArea.setText(finalCurrentUserList));
                        config.setUserList(currentUserList);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("User update listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in user update listening thread: " + e);
            }
        });
        userThread.start();
    }

    public void startListeningForGameStart(Stage currentStage) {
        new Thread(() -> {
            try {
                while (true) {
                    // Listen for any type of message
                    Object[] response = server.get(new ActualField("startGame"), new FormalField(String.class), new FormalField(String.class));
                    System.out.println("Received start game message");
                    // switch to game scene
                    if (response != null) {
                        // switch to game scene
                        Platform.runLater(() -> {
                            try {
                                Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/App_view.fxml")));
                                currentStage.setScene(new Scene(newRoot));

                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        break; // Stop listening for game start
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Updates listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in updates listening thread: " + e);
            }
        }).start();
    }
}
