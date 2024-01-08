package dk.dtu;

import dk.dtu.controller.PopUpController;
import org.jspace.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server implements Runnable {
    private final SpaceRepository repository;
    private final SequentialSpace chatSpace;
    private final String serverIp;
    private final Thread serverThread;

    public Server() throws UnknownHostException {
        serverIp = InetAddress.getLocalHost().getHostAddress();
        repository = new SpaceRepository();
        chatSpace = new SequentialSpace();
        serverThread = new Thread(this);
    }

    public void startServer() {
        serverThread.start();
    }

    @Override
    public void run() {
        try {
            repository.add("chat", chatSpace);
            String gateUri = "tcp://" + serverIp + ":9001/?keep";
            repository.addGate(gateUri);

            System.out.println("Chat server running at " + gateUri);

            Map<String, Set<String>> roomClients = new HashMap<>();

            while (!Thread.currentThread().isInterrupted()) {
                Object[] request = chatSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                String action = (String) request[0];
                String room = (String) request[1];
                String content = (String) request[2];

                roomClients.putIfAbsent(room, new HashSet<>());

                handleRequest(action, room, content, roomClients);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Server thread interrupted");
        } catch (Exception e) {
            System.out.println("Server error: " + e);
        }
    }

    private void updateUserList(String room, Map<String, Set<String>> roomClients) throws InterruptedException {
        Set<String> clients = roomClients.get(room);
        String userList = String.join("\n", clients);
        for (String clientID : clients) {
            chatSpace.put("users", room, clientID, userList);
        }
    }


    // Modify the handleRequest method
    private void handleRequest(String action, String room, String username, Map<String, Set<String>> roomClients) throws InterruptedException {
        switch (action) {
            case "join" -> {
                //Check the username is not already in the room
                if (roomClients.get(room).contains(username)) {
                    System.out.println("User " + username + " is already in room " + room);
                    PopUpController.showPopUp("Username already in use");
                    return;
                }
                roomClients.get(room).add(username);
                System.out.println("User " + username + " joined room " + room);
                updateUserList(room, roomClients); // Update user list
                broadcastMessage(room, username + " has joined the chat.\n", roomClients); // use username instead of content
            }
            case "leave" -> {
                roomClients.get(room).remove(username);
                System.out.println("User " + username + " left room " + room);
                updateUserList(room, roomClients); // Update user list
                broadcastMessage(room, username + " has left the chat.\n", roomClients); // use username instead of content
            }
            case "message" -> broadcastMessage(room, username, roomClients); // This should likely be changed as well
        }
    }


    // Add a new method to broadcast messages
    private void broadcastMessage(String room, String message, Map<String, Set<String>> roomClients) throws InterruptedException {
        for (String clientID : roomClients.get(room)) {
            chatSpace.put("message", room, clientID, message);
            System.out.println("Broadcasting message to " + clientID + " in room " + room);
            System.out.println("Message: " + message);
        }
    }

    public static void main(String[] args) {
        try {
            new Server().startServer();
        } catch (UnknownHostException e) {
            System.out.println("Could not determine local host IP");
            System.out.println("Error msg:" + e);
        }
    }
}
