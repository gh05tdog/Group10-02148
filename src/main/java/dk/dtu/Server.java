package dk.dtu;

import org.jspace.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Supplier;

public class Server implements Runnable {
    private final SpaceRepository repository;
    private final SequentialSpace gameSpace;
    private final String serverIp;
    private final Thread serverThread;
    private final Thread messageThread;
    private StatusControl statusControl;
    private final Set<String> playersInLobby;
    private boolean gameStarted;
    private boolean isDay = true; // Initial state
    private final List<String> messages = new ArrayList<>();
    private int timeSeconds = 10;
    private boolean isTimerRunning = false;
    private final Map<String, PlayerHandler> playerHandlers;
    private final Thread actionThread;

    private HashMap<String, String> voteMap;
    public String[] roleList;

    public Server() throws UnknownHostException {
        serverIp = InetAddress.getLocalHost().getHostAddress();
        repository = new SpaceRepository();
        actionThread = new Thread(this::runActionsListener);
        gameSpace = new SequentialSpace();
        serverThread = new Thread(this);
        playersInLobby = new HashSet<>();
        messageThread = new Thread(this::runMessageListener);
        playerHandlers = new HashMap<>();
        voteMap = new HashMap<>();

        gameStarted = false;
    }

    public void startServer() {
        serverThread.start();
        messageThread.start();
        actionThread.start();

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

    void handleMessage(String username, String messageContent) throws InterruptedException {
        // Add message to the list
        String fullMessage = username + ": " + messageContent ;
        messages.add(fullMessage);

        // Update the space with the new list of messages
        gameSpace.put("messages", messages);
    }

    void handleJoinLobby(String username) throws Exception {
        if (!gameStarted && !playersInLobby.contains(username)) {
            gameSpace.put("connected", username );
            playersInLobby.add(username);
            System.out.println("User " + username + " joined the lobby");
            messages.add(username + " joined the lobby");
            broadcastLobbyUpdate();


            // Create a new PlayerHandler for this player and start its thread
            PlayerHandler playerHandler = new PlayerHandler(username, playersInLobby.size() - 1, gameSpace);
            Thread playerThread = new Thread(playerHandler);
            playerHandlers.put(username, playerHandler); // Store the PlayerHandler
            playerThread.start();

        }else{
            throw new Exception("Game already started or user already in lobby");

        }
    }

    void handleLeaveLobby(String username) {
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

    void startGame() throws InterruptedException {
        if (!playersInLobby.isEmpty()) {
            broadcastToAllClients("startGame", "");
            gameSpace.put("gameStarted");
            assignRolesToPlayers();
            gameStarted = true;
            manageDayNightCycle();
            for(String username : playersInLobby){
                broadcastRoleUpdate(username);
            }
           statusControl = new StatusControl(playersInLobby.size(),roleList);

            System.out.println("Game is starting with players: " + playersInLobby);
        } else {
            System.out.println("Cannot start game with no players in lobby");
        }
    }

    private void assignRolesToPlayers() throws InterruptedException {
        if(!gameStarted){
            RandomSpace roles = new RandomSpace();
            roleList = new String[playersInLobby.size()];
            //int nrOfMafia = playersInLobby.size()/4;
            //for(int i = 0; i < nrOfMafia; i++){
            roles.put("Mafia");
            roles.put("Mafia");
            roles.put("Citizen");
            //}
            //roles.put("Bodyguard");
            //roles.put("Snitch");
            // for(int i = 0; i < playersInLobby.size() - nrOfMafia - 2; i++){
            roles.put("Citizen");
            // }

            for(String username : playersInLobby){
                playerHandlers.get(username).setRole(Arrays.toString(roles.get(new FormalField(String.class))));
                roleList[playerHandlers.get(username).getPlayerID()] = playerHandlers.get(username).getRole();
                System.out.println("Player " + username + "with ID: " + playerHandlers.get(username).getPlayerID() + " has role: " + playerHandlers.get(username).getRole());
            }
           System.out.println("Role list:" + Arrays.toString(roleList));

        }else{
            System.out.println("Game already started");
        }
    }

    public void broadcastRoleUpdate(String username) {
        try {
            gameSpace.put("roleUpdate", username, playerHandlers.get(username).getRole());

        } catch (InterruptedException e) {
            System.out.println("Error broadcasting role update: " + e);
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

    private void runActionsListener() {
        System.out.println("Action listener running");
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Object[] actionRequest = gameSpace.get(new ActualField("action"), new FormalField(String.class),new FormalField(String.class), new FormalField(String.class));
                String action = (String) actionRequest[1];
                String yourUsername = (String) actionRequest[2];
                String Victim = (String) actionRequest[3];
                if(!isDay){
                    switch (action) {
                        case "MafiaVote" -> mafiaVote(yourUsername, Victim);
                        case "Snitch" -> snitchAction(yourUsername, Victim);
                        case "Bodyguard" -> bodyguardAction(yourUsername, Victim);
                    }
                }


            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Message listener thread interrupted");
        } catch (Exception e) {
            System.out.println("Message listener error: " + e);
        }
    }

    private void bodyguardAction(String yourUsername, String victim) {
    }

    private void snitchAction(String yourUsername, String victim) {
    }

    private void mafiaVote(String yourUsername, String victim) throws InterruptedException {
        int nrOfMafia = 2;
        System.out.println(playersInLobby);
        voteMap.put(yourUsername, victim);
        if (nrOfMafia == voteMap.size()) {
            // Map to keep track of vote counts
            HashMap<String, Integer> voteCount = new HashMap<>();

            // Count the votes for each user
            for (String vote : voteMap.values()) {
                voteCount.put(vote, voteCount.getOrDefault(vote, 0) + 1);
            }

            // Determine if all votes are different or find the user with the most votes
            String mostVotedUser = null;
            int maxVotes = 0;

            for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
                if (entry.getValue() > maxVotes) {
                    maxVotes = entry.getValue();
                    mostVotedUser = entry.getKey();
                }
            }

            if(nrOfMafia == 1){
                System.out.println("Mafia eliminated: " + mostVotedUser);
                voteCount.clear();
                statusControl.attemptMurder(playerHandlers.get(mostVotedUser).getPlayerID());
                System.out.println(statusControl.conductor[playerHandlers.get(mostVotedUser).getPlayerID()].isKilled());
                return;
            }

            voteCount.clear();
            if (maxVotes == 1) {
                // If all values are different, everyone has 1 vote
                System.out.println("All users have 1 vote.");
            } else {
                // Otherwise, print the victim with the most votes
                System.out.println("Victim with the most votes: " + mostVotedUser);
                statusControl.attemptMurder(playerHandlers.get(mostVotedUser).getPlayerID());
                System.out.println(statusControl.conductor[playerHandlers.get(mostVotedUser).getPlayerID()].isKilled());
            }
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

    void manageDayNightCycle() {
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
                        try {
                            broadcastDayNightCycle();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }, 0, 1000); // Update every second
        }
    }

    void broadcastDayNightCycle() throws InterruptedException {
        String state = isDay ? "day" : "night";
        if (isDay) {
            messages.clear();
            voteMap.clear();
            for (int i=0; i < playersInLobby.size(); i++) {
                gameSpace.put("messages", messages);
            }
        }
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

    public void stopServer() {
        serverThread.interrupt();
        messageThread.interrupt();
    }

    public boolean isRunning() {
        return serverThread.isAlive();
    }

    public String getPlayersInLobby() {
        return playersInLobby.toString();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public  List<String>  getMessages() {
        return messages;
    }

    public Boolean getDayNightCycle() {
        return isDay;
    }
}
