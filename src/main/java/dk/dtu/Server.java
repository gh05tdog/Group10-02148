package dk.dtu;

import org.jspace.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLOutput;
import java.util.*;

public class Server implements Runnable {
    private final SpaceRepository repository;
    private final SequentialSpace gameSpace;
    private final String serverIp;
    private final Thread serverThread;
    private final Thread messageThread;
    private StatusControl statusControl;
    private final Set<String> playersInLobby;
    private boolean gameStarted;
    private String stageCycle = "Day"; // Initial state

    private final List<String> messages = new ArrayList<>();
    private int timeSeconds = 10;
    private boolean isTimerRunning = false;
    private final Map<String, PlayerHandler> playerHandlers;
    private final Thread actionThread;

    private HashMap<String, String> mafiaVoteMap;

    private HashMap<String, String> executeVoteMap;
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
        executeVoteMap = new HashMap<>();
        mafiaVoteMap = new HashMap<>();

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
        String fullMessage = username + ": " + messageContent;
        messages.add(fullMessage);

        // Update the space with the new list of messages
        gameSpace.put("messages", messages);
    }

    void handleJoinLobby(String username) throws Exception {
        if (!gameStarted && !playersInLobby.contains(username)) {
            gameSpace.put("connected", username);
            playersInLobby.add(username);
            System.out.println("User " + username + " joined the lobby");
            messages.add(username + " joined the lobby");
            broadcastLobbyUpdate();


            // Create a new PlayerHandler for this player and start its thread
            PlayerHandler playerHandler = new PlayerHandler(username, playersInLobby.size() - 1, gameSpace);
            Thread playerThread = new Thread(playerHandler);
            playerHandlers.put(username, playerHandler); // Store the PlayerHandler
            playerThread.start();

        } else {
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
            for (String username : playersInLobby) {
                System.out.println("Sending role update to: " + username);
                broadcastRoleUpdate(username);
            }
            statusControl = new StatusControl(playersInLobby.size(), roleList);

            System.out.println("Game is starting with players: " + playersInLobby);
        } else {
            System.out.println("Cannot start game with no players in lobby");
        }
    }

    private void assignRolesToPlayers() throws InterruptedException {
        if (!gameStarted) {
            RandomSpace roles = new RandomSpace();
            roleList = new String[playersInLobby.size()];
            //int nrOfMafia = playersInLobby.size()/4;
            //for(int i = 0; i < nrOfMafia; i++){
            roles.put("Mafia");
            //roles.put("Snitch");
            roles.put("Bodyguard");
            //roles.put("Mafia");
            //}
            //roles.put("Bodyguard");
            roles.put("Snitch");
            // for(int i = 0; i < playersInLobby.size() - nrOfMafia - 2; i++){
            //roles.put("Citizen");
            // }

            for (String username : playersInLobby) {
                playerHandlers.get(username).setRole(Arrays.toString(roles.get(new FormalField(String.class))));
                roleList[playerHandlers.get(username).getPlayerID()] = playerHandlers.get(username).getRole();
                System.out.println("Player " + username + "with ID: " + playerHandlers.get(username).getPlayerID() + " has role: " + playerHandlers.get(username).getRole());
            }
            System.out.println("Role list:" + Arrays.toString(roleList));

        } else {
            System.out.println("Game already started");
        }
    }

    public void broadcastRoleUpdate(String username) {
        try {
            gameSpace.put("roleUpdate", username, playerHandlers.get(username).getRole());
            System.out.println("Role update sent to: " + username + "with role: " + playerHandlers.get(username).getRole());

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
                Object[] messageRequest = gameSpace.get(new ActualField("message"), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
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
                Object[] actionRequest = gameSpace.get(new ActualField("action"), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                String action = (String) actionRequest[1];
                String yourUsername = (String) actionRequest[2];
                String Victim = (String) actionRequest[3];
                if (Objects.equals(stageCycle, "Night")) {
                    switch (action) {
                        case "MafiaVote" -> mafiaVote(yourUsername, Victim);
                        case "Snitch" -> snitchAction(yourUsername, Victim);
                        case "Bodyguard" -> bodyguardAction(yourUsername, Victim);
                    }
                } else if (Objects.equals(stageCycle, "VotingTime")) {
                    executeVote(yourUsername, Victim);
                } else {
                    System.out.println("Not the right time to do that");

                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Message listener thread interrupted");
        } catch (Exception e) {
            System.out.println("Message listener error: " + e);
        }
    }

    private void executeVote(String yourUsername, String suspect) throws InterruptedException {
        if (Objects.equals(yourUsername, suspect)) {
            System.out.println("Dont vote for yourself, dummy!");
            return;
        } else {executeVoteMap.put(yourUsername, suspect);
            System.out.println("Vote received from: " + yourUsername + " on: " + suspect);}

        HashMap<String, Integer> executeVoteCount = new HashMap<>();

        for (String vote : executeVoteMap.values()) {
            executeVoteCount.put(vote, executeVoteCount.getOrDefault(vote, 0) + 1);
        }
        String mostVotedUser = null;
        int maxVotes = 0;

        for (Map.Entry<String, Integer> entry : executeVoteCount.entrySet()) {
            if (entry.getValue() > maxVotes) {
                maxVotes = entry.getValue();
                mostVotedUser = entry.getKey();
            }
        }

        int divided = (playersInLobby.size()/2)+1;

        if (maxVotes >= divided) {
            System.out.println("The town eliminated: " + mostVotedUser);
            executeVoteCount.clear();
            statusControl.executeSuspect(playerHandlers.get(mostVotedUser).getPlayerID());
            broadcastToAllClients("mafiaEliminated", mostVotedUser);
            System.out.println(statusControl.conductor[playerHandlers.get(mostVotedUser).getPlayerID()].isKilled());
            return;
        }


    }

    private void bodyguardAction(String yourUsername, String victim) throws InterruptedException {
        System.out.println("Protecting player: " + victim);
        statusControl.protectPlayer(playerHandlers.get(victim).getPlayerID());
        System.out.println("BODYGUARD: " + statusControl.conductor[playerHandlers.get(victim).getPlayerID()].isSecured());
    }

    private void snitchAction(String yourUsername, String victim) throws InterruptedException {
        System.out.println("Snitching on player: " + victim);
        System.out.println("SNITCH: " + statusControl.getPlayerRole(playerHandlers.get(victim).getPlayerID()));
        if (!(statusControl.conductor[playerHandlers.get(victim).getPlayerID()].isSecured())) {
            broadCastToSnitch(yourUsername, victim, playerHandlers.get(victim).getRole());
        } else {
            System.out.println("Snitching failed");
        }

    }


    private void mafiaVote(String yourUsername, String victim) throws InterruptedException {
        int nrOfMafia = 1;
        System.out.println(playersInLobby);
        mafiaVoteMap.put(yourUsername, victim);
        if (nrOfMafia == mafiaVoteMap.size()) {
            // Map to keep track of vote counts
            HashMap<String, Integer> voteCount = new HashMap<>();

            // Count the votes for each user
            for (String vote : mafiaVoteMap.values()) {
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

            if (nrOfMafia == 1) {
                System.out.println("Mafia eliminated: " + mostVotedUser);
                voteCount.clear();
                statusControl.attemptMurder(playerHandlers.get(mostVotedUser).getPlayerID());
                if ((statusControl.conductor[playerHandlers.get(mostVotedUser).getPlayerID()].isKilled())) {
                    broadcastToAllClients("mafiaEliminated", mostVotedUser);
                } else {
                    System.out.println("Mafia kill failed");
                }

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
                if (statusControl.conductor[playerHandlers.get(mostVotedUser).getPlayerID()].isKilled()) {
                    broadcastToAllClients("mafiaEliminated", mostVotedUser);
                } else {
                    System.out.println("Mafia kill failed");
                }
            }
        }
    }

    public void broadCastToSnitch(String username, String victimRole, String victimUsername) throws InterruptedException {
        if (playerHandlers.get(username).getRole().equals("[Snitch]")) {
            System.out.println("Sending snitch message to: " + username);
            gameSpace.put("snitchMessage", username, playerHandlers.get(username).getRole(), victimUsername, victimRole);
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
                        switch (stageCycle) {
                            case "Day" -> {
                                stageCycle = "VotingTime";
                                System.out.println(stageCycle);
                                timeSeconds = 10; // Reset timer
                                try {
                                    broadcastDayNightCycle();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case "Night" -> {
                                stageCycle = "Day";
                                timeSeconds = 10; // Reset timer
                                System.out.println(stageCycle);
                                try {
                                    broadcastDayNightCycle();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case "VotingTime" -> {
                                stageCycle = "Night";
                                timeSeconds = 5; // Reset timer
                                System.out.println(stageCycle);
                                try {
                                    broadcastDayNightCycle();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            }, 0, 1000); // Update every second
        }
    }

    void broadcastDayNightCycle() throws InterruptedException {
        if (Objects.equals(stageCycle, "Day")) {
            messages.clear();
            mafiaVoteMap.clear();
            for (int i = 0; i < playersInLobby.size(); i++) {
                gameSpace.put("messages", messages);
            }
        } else if (Objects.equals(stageCycle, "Night")) {
            messages.clear();
            executeVoteMap.clear();
            for (int i = 0; i < playersInLobby.size(); i++) {
                gameSpace.put("messages", messages);
            }
        }
        broadcastToAllClients("dayNightCycle", stageCycle);
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

    public List<String> getMessages() {
        return messages;
    }

    public String stageCycle() {
        return stageCycle;
    }

}
