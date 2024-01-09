package dk.dtu;

import dk.dtu.controller.PopUpController;
import org.jspace.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Server implements Runnable {
    private final SpaceRepository repository;
    private final SequentialSpace gameSpace;
    private final String serverIp;
    private final Thread serverThread;
    private final Thread messageThread;
    private final Set<String> playersInLobby;
    private boolean gameStarted;
    private List<String> messages = new ArrayList<>();

    public Server() throws UnknownHostException {
        serverIp = InetAddress.getLocalHost().getHostAddress();
        repository = new SpaceRepository();
        gameSpace = new SequentialSpace();
        serverThread = new Thread(this);
        playersInLobby = new HashSet<>();
        messageThread = new Thread(this::runMessageListener);

        gameStarted = false;
    }

    public void startServer() {
        serverThread.start();
        messageThread.start();
    }

    @Override
    public void run() {
        try {
            repository.add("game", gameSpace);
            String gateUri = "tcp://" + serverIp + ":9001/?keep";
            repository.addGate(gateUri);

            System.out.println("Game server running at " + gateUri);

            while (!Thread.currentThread().isInterrupted()) {
                Object[] request = gameSpace.get(new FormalField(String.class), new FormalField(String.class));
                String action = (String) request[0];
                String username = (String) request[1];

                switch (action) {
                    case "joinLobby" -> handleJoinLobby(username);
                    case "leaveLobby" -> handleLeaveLobby(username);
                    case "startGame" -> startGame();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Server thread interrupted");
        } catch (Exception e) {
            System.out.println("Server error: " + e);
        }
    }

    private void handleMessage(String username, String messageContent, String lobbyID) throws InterruptedException {
        // Add message to the list
        String fullMessage = "(" + lobbyID + ")" + username + ": " + messageContent ;
        messages.add(fullMessage);

        // Update the space with the new list of messages
        gameSpace.put("messages", messages);
    }

    private void handleJoinLobby(String username) {
        if (!gameStarted) {
            if (!playersInLobby.contains(username)) {
                playersInLobby.add(username);
                System.out.println("User " + username + " joined the lobby");
                // Notify players of the new user in the lobby
                broadcastLobbyUpdate();
            } else {
                PopUpController.showPopUp("Username already in use");
            }
        } else {
            PopUpController.showPopUp("Game already started");
        }
    }

    private void handleLeaveLobby(String username) {
        if (playersInLobby.remove(username)) {
            System.out.println("User " + username + " left the lobby");
            broadcastLobbyUpdate();
        }
    }

    private void startGame() {
        if (!playersInLobby.isEmpty()) {
            gameStarted = true;
            System.out.println("Game is starting with players: " + playersInLobby);
            // Initialize game state and broadcast start message
        } else {
            System.out.println("Cannot start game with no players in lobby");
        }
    }

    private void broadcastLobbyUpdate() {
        try {
            gameSpace.put("lobbyUpdate", String.join(", ", playersInLobby));
        } catch (InterruptedException e) {
            System.out.println("Error broadcasting lobby update: " + e);
        }
    }

    private void runMessageListener() {
        System.out.println("Message listener running");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Object[] messageRequest = gameSpace.get(new ActualField("message"),new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                String username = (String) messageRequest[1];
                String message = (String) messageRequest[2];
                String lobbyID = (String) messageRequest[3];
                handleMessage(username, message, lobbyID);
                System.out.println("Message received: " + message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Message listener thread interrupted");
        } catch (Exception e) {
            System.out.println("Message listener error: " + e);
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
