package dk.dtu.model;

import dk.dtu.config;
import dk.dtu.controller.AppController;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppModel {
    private final RemoteSpace server;

    public AppModel() throws IOException {
        this.server = new RemoteSpace(config.getIp() + "/chat?keep");
    }

    public void sendMessage(String clientID, String message, String roomId) throws InterruptedException {
        server.put("message", clientID, message, roomId);
        System.out.println("Sent message: " + message);
    }

    public void joinLobby(String userName) throws InterruptedException {
        server.put("joinLobby", userName);
    }

    public void startListeningForMessages(TextArea messageAreaLobby) {
        // Update last seen messages
        Thread messageThread = new Thread(() -> {
            try {
                List<String> lastSeenMessages = new ArrayList<>();
                while (true) {
                    Object[] response = server.query(new ActualField("messages"), new FormalField(List.class));
                    // Create a list of type list any to hold the response
                    List<?> rawList = (List<?>) response[1];
                    // Check if the list contains only strings
                    if (rawList.stream().allMatch(item -> item instanceof String)) {
                        // Convert the list to a list of strings
                        List<String> newMessages = rawList.stream().map(Object::toString).toList();
                        if (!response[1].equals(lastSeenMessages)) {
                            Platform.runLater(() -> {
                                // Clear the message area and add the new messages
                                messageAreaLobby.clear();
                                for (String msg : newMessages) {
                                    messageAreaLobby.appendText(msg + "\n");
                                }
                            });
                            lastSeenMessages = new ArrayList<>(newMessages); // Update last seen messages
                        }
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Message listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in message listening thread: " + e);
            }
        });
        messageThread.start();
    }

    public void listenforRoleUpdate(AppController appController, String username) {
        Thread roleThread = new Thread(() -> {
            try {
                while (true) {
                    // Retrieve the user list update intended for this client
                    Object[] response = server.get(new ActualField("roleUpdate"), new ActualField(username), new FormalField(String.class));
                    if (response != null) {
                        // Update the user list
                        String role = (String) response[2];
                        System.out.println(username + " has role: " + role);
                        Platform.runLater(() -> appController.appendRoles(role));
                        config.setRole(role);
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("User update listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in user update listening thread: " + e);
            }
        });
        roleThread.start();
    }


    public void startListeningForDayNightCycle(AppController appController, String Username) {
        new Thread(() -> {
            try {
                while (true) {
                    // Listen for any type of message
                    Object[] response = server.get(new FormalField(String.class), new ActualField(Username), new FormalField(String.class));
                    String messageType = (String) response[0];
                    String messageContent = (String) response[2];
                    if ("dayNightCycle".equals(messageType)) {
                        Platform.runLater(() -> appController.updateDayNightCycle(messageContent));
                    } else if ("timeUpdate".equals(messageType)) {
                        Platform.runLater(() -> appController.updateTimeLabel(messageContent));
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("Updates listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in updates listening thread: " + e);
            }
        }).start();
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

    public void startGame() throws InterruptedException {
        //Send message to server to start game
        server.put("startGame", "some_identifier_or_info");
    }

    public void startListeningForUserUpdates(TextArea userListArea, String clientID) {
        // Retrieve the user list update intended for this client
        // Update the user list
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
}
