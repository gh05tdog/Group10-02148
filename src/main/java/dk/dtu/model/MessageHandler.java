package dk.dtu.model;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.util.ArrayList;
import java.util.List;

public class MessageHandler {
    private final RemoteSpace server;
    private List<String> lastSeenMessages;

    public MessageHandler(RemoteSpace server) {
        this.server = server;
        this.lastSeenMessages = new ArrayList<>();
    }

    public void sendMessage(String clientID, String message, String roomId) throws InterruptedException {
        //Make sure the message is not too long
        if (message.length() > 100) {
            message = message.substring(0, 100);
        }
        server.put("message", clientID, message, roomId);
    }

    public void startListeningForMessages(TextArea messageArea) {
        // Update last seen messages
        Thread messageThread = new Thread(() -> {
            try {
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
                                messageArea.clear();
                                for (String msg : newMessages) {
                                    messageArea.appendText(msg + "\n");
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
}
