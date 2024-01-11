package dk.dtu;

import org.jspace.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Server implements Runnable {
    private final SpaceRepository repository;
    private final SequentialSpace gameSpace;
    private final String serverIp;
    private final Thread serverThread;
    private final Thread messageThread;
    private final Set<String> playersInLobby;
    private boolean gameStarted;
    private boolean isDay = true; // Initial state
    private final List<String> messages = new ArrayList<>();
    private int timeSeconds = 10;
    private boolean isTimerRunning = false;
    private final Map<String, PlayerHandler> playerHandlers;

    public Server() throws UnknownHostException {
        serverIp = InetAddress.getLocalHost().getHostAddress();
        repository = new SpaceRepository();
        gameSpace = new SequentialSpace();
        serverThread = new Thread(this);
        playersInLobby = new HashSet<>();
        messageThread = new Thread(this::runMessageListener);
        playerHandlers = new HashMap<>();

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

    private void handleMessage(String username, String messageContent) throws InterruptedException {
        // Add message to the list
        String fullMessage = username + ": " + messageContent ;
        messages.add(fullMessage);

        // Update the space with the new list of messages
        gameSpace.put("messages", messages);
    }

    private void handleJoinLobby(String username) throws Exception {
        if (!gameStarted && !playersInLobby.contains(username)) {
            gameSpace.put("connected", username );
            playersInLobby.add(username);
            System.out.println("User " + username + " joined the lobby");
            messages.add(username + " joined the lobby");
            broadcastLobbyUpdate();

            // Create a new PlayerHandler for this player and start its thread
            PlayerHandler playerHandler = new PlayerHandler(username, playersInLobby.size(), gameSpace);
            Thread playerThread = new Thread(playerHandler);
            playerHandlers.put(username, playerHandler); // Store the PlayerHandler
            playerThread.start();

        }else{
            throw new Exception("Game already started or user already in lobby");

        }
    }

    private void handleLeaveLobby(String username) {
        if (playersInLobby.remove(username)) {
            System.out.println("User " + username + " left the lobby");
            broadcastLobbyUpdate();

            // Stop the player's handler
            if (playerHandlers.containsKey(username)) {
                PlayerHandler playerHandler = playerHandlers.get(username);
                playerHandler.stop(); // Stop the PlayerHandler
                playerHandlers.remove(username);
            }
        }
    }

    private void startGame() throws InterruptedException {
        if (!playersInLobby.isEmpty()) {
            broadcastToAllClients("startGame", "");
            gameSpace.put("gameStarted");
            gameStarted = true;
            manageDayNightCycle();
            System.out.println("Game is starting with players: " + playersInLobby);
        } else {
            System.out.println("Cannot start game with no players in lobby");
        }
    }

    private void broadcastLobbyUpdate() {
        try {
            String userList = String.join(", ", playersInLobby);

            // Broadcast the user list to each user in the lobby
            for (String user : playersInLobby) {
                gameSpace.put("userUpdate", user, userList);
            }
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
                handleMessage(username, message);
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

    private void manageDayNightCycle() {
        if (!isTimerRunning) {
            isTimerRunning = true;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {

                    if (timeSeconds > 0) {
                        timeSeconds--;
                        broadcastTimeUpdate(timeSeconds);
                    } else {
                        isDay = !isDay; // Toggle state
                        timeSeconds = 10; // Reset timer
                        broadcastDayNightCycle();
                    }
                }
            }, 0, 1000); // Update every second
        }
    }

    private void broadcastDayNightCycle() {
        String state = isDay ? "day" : "night";
        broadcastToAllClients("dayNightCycle", state);
    }

    private void broadcastTimeUpdate(int timeSeconds) {
        // Format time as needed
        String time = String.format("%02d:%02d", timeSeconds / 60, timeSeconds % 60);
        broadcastToAllClients("timeUpdate", time);
    }

    private void broadcastToAllClients(String messageType, String messageContent) {
        try {
            for (String username : playersInLobby) {
                gameSpace.put(messageType, username, messageContent);
            }
        } catch (InterruptedException e) {
            System.out.println("Error broadcasting message: " + e.getMessage());
            // Handle the exception appropriately
        }
    }

}
