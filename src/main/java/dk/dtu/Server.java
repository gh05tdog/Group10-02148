package dk.dtu;

import org.jspace.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Server implements Runnable {
    private static SequentialSpace gameSpace;
    private static Thread serverThread;
    private static Thread messageThread;
    private static Thread actionThread;
    private final SpaceRepository repository;
    private final String serverIp;
    private final List<String> messages = new ArrayList<>();
    private final HashMap<String, String> mafiaVoteMap;
    private final HashMap<String, String> executeVoteMap;
    public String[] roleList;
    public String[] nameList;
    public IdentityProvider identityProvider = new IdentityProvider();
    private StatusControl statusControl;
    private boolean gameStarted;
    private String stageCycle = "Day"; // Initial state
    private int timeSeconds = 30;
    private boolean isTimerRunning = false;

    public Server() throws UnknownHostException {
        serverIp = InetAddress.getLocalHost().getHostAddress();
        repository = new SpaceRepository();
        actionThread = new Thread(this::runActionsListener);
        gameSpace = new SequentialSpace();
        serverThread = new Thread(this);
        messageThread = new Thread(this::runMessageListener);
        executeVoteMap = new HashMap<>();
        mafiaVoteMap = new HashMap<>();
        gameStarted = false;
    }


    public static void main(String[] args) {
        try {
            new Server().startServer();
        } catch (UnknownHostException e) {
            System.out.println("Could not determine local host IP");
            System.out.println("Error msg:" + e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    //Used in tests to stop the server
    public static void stopServer() {
        Server.serverThread.interrupt();
        serverThread.interrupt();
        messageThread.interrupt();
        actionThread.interrupt();
        gameSpace.getAll();
    }

    //Used to start the server
    public void startServer() throws InterruptedException {
        gameSpace.put("lock");
        serverThread.start();
        messageThread.start();
        actionThread.start();

    }

    @Override
    public void run() {
        try {
            // Create a repository for the gameSpace
            repository.add("game", gameSpace);
            String gateUri = "tcp://" + serverIp + ":9001/?keep";

            // Create the gate and register the gameSpace to it
            repository.addGate(gateUri);

            System.out.println("Game server running at " + gateUri);

            while (!Thread.currentThread().isInterrupted()) {
                Object[] request = gameSpace.get(new FormalField(String.class), new FormalField(String.class));
                String action = (String) request[0];
                String username = (String) request[1];

                // Handle the request based on the action
                switch (action) {
                    case "joinLobby" -> handleJoinLobby(username);
                    case "startGame" -> startGame();
                    case "usernameCheck" -> checkUsername(username);
                    case "leaveLobby" -> {
                        identityProvider.removePlayer(username);
                        messages.add(username + " left the lobby");
                        broadcastLobbyUpdate();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Server thread interrupted");
        } catch (Exception e) {
            System.out.println("Server error: " + e);
        }
    }

    // Check if the username is already in use by another player
    // And send the result back to the client
    private void checkUsername(String username) {
        try {
            //Check if the username is already in use
            if (identityProvider.isPlayerInLobby(username)) {
                gameSpace.put("usernameCheck", false);
            } else {
                gameSpace.put("usernameCheck", true);
            }
        } catch (InterruptedException e) {
            System.out.println("Error checking username: " + e);
        }
    }


    // Handle a message from a client and add it to the list of messages in the gameSpace
    void handleMessage(String username, String messageContent) throws InterruptedException {
        // Add message to the list
        String fullMessage = username + ": " + messageContent;
        messages.add(fullMessage);
        // Update the space with the new list of messages
        gameSpace.put("messages", messages);
    }


    // Handle a request to join the lobby
    void handleJoinLobby(String username) throws Exception {
        if (!gameStarted && !identityProvider.isPlayerInLobby(username)) {
            gameSpace.put("connected", username);
            identityProvider.addPlayer(username);
            messages.add(username + " joined the lobby");
            broadcastLobbyUpdate();
        } else {
            throw new Exception("Game already started or user already in lobby");
        }
    }

    // Handle a request to start the game and assign roles to players
    void startGame() throws InterruptedException {
        broadcastToAllClients("startGame", "");
        assignRolesToPlayers();
        gameStarted = true;
        manageDayNightCycle();
        statusControl = new StatusControl(identityProvider.getNumberOfPlayersInLobby(), nameList, roleList);
        for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
            broadcastRoleUpdate(identityProvider.getPlayersInLobby().toArray()[i].toString(), i);
        }
        messages.clear();

    }

    private void assignRolesToPlayers() throws InterruptedException {
        if (!gameStarted) {
            // Create a random space to randomly assign roles to players
            RandomSpace roles = new RandomSpace();
            // Create a list of roles
            roleList = new String[identityProvider.getNumberOfPlayersInLobby()];
            nameList = new String[identityProvider.getNumberOfPlayersInLobby()];

            // Add roles to the list
            int nrOfMafia = identityProvider.getNumberOfPlayersInLobby() / 4;
            for (int i = 0; i < nrOfMafia; i++) {
                roles.put("Mafia");
            }
            roles.put("Snitch");
            roles.put("Bodyguard");

            for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby() - nrOfMafia - 2; i++) {
                roles.put("Citizen");
            }

            //Assign roles to players
            for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
                String role = Arrays.toString(roles.get(new FormalField(String.class)));
                roleList[i] = role;
                nameList[i] = identityProvider.getPlayersInLobby().toArray()[i].toString();
            }

        }
    }

    // Broadcast the role of a player to all clients
    public void broadcastRoleUpdate(String username, int playerID) {
        try {
            gameSpace.put("roleUpdate", username, statusControl.getPlayerRole(playerID));
        } catch (InterruptedException e) {
            System.out.println("Error broadcasting role update: " + e);
        }
    }

    // Broadcast the list of users in the lobby to all clients
    private void broadcastLobbyUpdate() {
        try {
            String userList = String.join(", ", identityProvider.getPlayersInLobby());

            // Broadcast the user list to each user in the lobby
            for (String user : identityProvider.getPlayersInLobby()) {
                gameSpace.put("userUpdate", user, userList);
            }
        } catch (InterruptedException e) {
            System.out.println("Error broadcasting lobby update: " + e);
        }
    }


    // Listen for messages from clients and add them to the list of messages in the gameSpace
    private void runMessageListener() {
        try {
            String gateUri = serverIp + ":9001";
            messages.add("Game server running at " + gateUri);
            gameSpace.put("messages", messages);
            while (!Thread.currentThread().isInterrupted()) {
                Object[] messageRequest = gameSpace.get(new ActualField("message"), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                String username = (String) messageRequest[1];
                String message = (String) messageRequest[2];

                // Send the message to the client
                handleMessage(username, message);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Message listener thread interrupted");
        } catch (Exception e) {
            System.out.println("Message listener error: " + e);
        }
    }


    // Listen for actions from clients and execute them
    private void runActionsListener() {
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
                    // if voting time, continually run executeVote, checking for votes
                    executeVote(yourUsername, Victim);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Message listener thread interrupted");
        } catch (Exception e) {
            System.out.println("Message listener error: " + e);
        }
    }


    void executeVote(String yourUsername, String suspect) throws InterruptedException {
        // Check if the player is alive
        if (!statusControl.conductor[statusControl.getIDFromUserName(yourUsername)].isKilled()) {
            // Check if the player is voting for themselves
            if (Objects.equals(yourUsername, suspect)) {
                return;
            } else {
                // Add the vote to the map
                executeVoteMap.put(yourUsername, suspect);
            }
            // create a map for counting votes
            HashMap<String, Integer> executeVoteCount = new HashMap<>();
            handleVotes(executeVoteCount, executeVoteMap);

            String mostVotedUser = null;
            int maxVotes = 0;

            // Determine if all votes are different or find the user with the most votes
            for (Map.Entry<String, Integer> entry : executeVoteCount.entrySet()) {
                if (entry.getValue() > maxVotes) {
                    maxVotes = entry.getValue();
                    mostVotedUser = entry.getKey();
                }

                int alivePlayers = 0;
                // check the number of alive players
                for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
                    if (!statusControl.conductor[i].isKilled()) {
                        alivePlayers++;
                    }
                }
                int divided = (alivePlayers / 2) + 1;
                // check if the number of votes is enough to execute
                if (maxVotes >= divided) {
                    executeVoteCount.clear();
                    // send a request to status control to execute the player
                    statusControl.executeSuspect(statusControl.getIDFromUserName(mostVotedUser));
                    // use the mafiaEliminated message to update the client
                    broadcastToAllClients("mafiaEliminated", mostVotedUser);
                    // Check if any win condition has been met
                    checkForVictory();
                }
            }
        }
    }

    private void handleVotes(HashMap<String, Integer> votes, HashMap<String, String> votesMap) {
        for (String vote : votesMap.values()) {
            votes.put(vote, votes.getOrDefault(vote, 0) + 1);
        }
    }

    // Handle the bodyguard action and protect a player
    private void bodyguardAction(String yourUsername, String victim) throws InterruptedException {
        if (!statusControl.conductor[statusControl.getIDFromUserName(yourUsername)].isKilled()) {
            statusControl.protectPlayer(statusControl.getIDFromUserName(victim));
        }
    }

    // Handle the snitch action and reveal the role of a player
    private void snitchAction(String yourUsername, String victim) throws InterruptedException {
        if (!statusControl.conductor[statusControl.getIDFromUserName(yourUsername)].isKilled()) {
            if (!(statusControl.conductor[statusControl.getIDFromUserName(victim)].isSecured())) {
                broadCastToSnitch(yourUsername, victim, statusControl.getPlayerRole(statusControl.getIDFromUserName(victim)));
            } else {
                // If the player is protected, the snitch will not know their role
                broadCastToSnitch(yourUsername, victim, "[REDACTED]");
            }
        }
    }


    //This function is used to handle the mafia vote
    private void mafiaVote(String yourUsername, String victim) throws InterruptedException {
        //First gets the number of mafia players alive
        if (!statusControl.conductor[statusControl.getIDFromUserName(yourUsername)].isKilled()) {
            int nrOfMafia = 0;
            for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
                if (statusControl.conductor[i].isKilled()) continue; // Skip dead players

                if (statusControl.conductor[i].getRole().contains("Mafia")) {
                    nrOfMafia++;
                }
            }

            //PUts each mafiavote into a hashmap with the mafia player as key and the victim as value
            mafiaVoteMap.put(yourUsername, victim);

            //Once all mafia players have voted, count the votes for each player and put it into voteCount map
            if (nrOfMafia == mafiaVoteMap.size()) {

                HashMap<String, Integer> voteCount = new HashMap<>();

                String mostVotedUser = null;
                int maxVotes = 0;

                // Count the votes for each user
                handleVotes(voteCount, mafiaVoteMap);

                //Sets the maxVotes to the max amount of votes for a player and the mostVotedUser to the player with the most votes
                for (Map.Entry<String, Integer> entry : voteCount.entrySet()) {
                    if (entry.getValue() > maxVotes) {
                        maxVotes = entry.getValue();
                        mostVotedUser = entry.getKey();
                    }
                }

                //If there is only 1 mafia in the game left, the mafia will automatically kill the player with the most votes
                if (nrOfMafia == 1) {
                    voteCount.clear();
                    statusControl.attemptMurder(statusControl.getIDFromUserName(mostVotedUser));

                    //This broadcaststhe message to the client for it to handle and checks if the game has endted
                    if ((statusControl.conductor[statusControl.getIDFromUserName(mostVotedUser)].isKilled())) {
                        broadcastToAllClients("mafiaEliminated", mostVotedUser);
                        checkForVictory();
                    }
                    return;
                }

                voteCount.clear();
                if (!(maxVotes == 1)) {
                    //If there isn't a tie, meaning someone has the most votes, the mafia will kill that player just like it does above with only 1 mafia
                    statusControl.attemptMurder(statusControl.getIDFromUserName(mostVotedUser));
                    if (statusControl.conductor[statusControl.getIDFromUserName(mostVotedUser)].isKilled()) {
                        broadcastToAllClients("mafiaEliminated", mostVotedUser);
                        checkForVictory();
                    }

                }
            }
        }
    }

    // Broadcast a message to the snitch
    public void broadCastToSnitch(String username, String victimRole, String victimUsername) throws InterruptedException {
        if (statusControl.getPlayerRole(statusControl.getIDFromUserName(username)).contains("Snitch")) {
            gameSpace.put("snitchMessage", username, statusControl.getPlayerRole(statusControl.getIDFromUserName(username)), victimUsername, victimRole);
        }
    }

    // End the game and broadcast the result to all clients
    private void endGame(String message) {
        //Stop timer
        isTimerRunning = false;
        broadcastToAllClients("gameEnd", message);
    }

    // Check if the game has ended
    private void checkForVictory() {
        int mafiaCount = 0;
        int nonMafiaCount = 0;

        // Count the number of mafia and non-mafia players
        for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
            if (statusControl.conductor[i].isKilled()) continue; // Skip dead players

            if (statusControl.conductor[i].getRole().contains("Mafia")) {
                mafiaCount++;
            } else {
                nonMafiaCount++;
            }
        }


        // Check if the game has ended
        if (mafiaCount >= nonMafiaCount) {
            endGame("Mafia wins!");
        } else if (mafiaCount == 0) {
            endGame("Citizens win!");
        }
    }

    void manageDayNightCycle() {
        // Start the timer
        isTimerRunning = true;
        new Timer().schedule(new TimerTask() {
            @Override
            // Use the timer to cycle between 3 different stages.
            public void run() {
                if (isTimerRunning) {
                    if (timeSeconds > 0) {
                        timeSeconds--;
                        broadcastTimeUpdate(timeSeconds);
                    } else {
                        // If the stage is day, when the timer runs out, switch to voting time,
                        // and set the timer to 15 seconds
                        switch (stageCycle) {
                            case "Day" -> {
                                stageCycle = "VotingTime";
                                timeSeconds = 15; // Reset timer
                                try {
                                    broadcastDayNightCycle();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // If the stage is night, when the timer runs out, switch to day,
                            // clear all messages, and set the timer to 30 seconds
                            case "Night" -> {
                                stageCycle = "Day";
                                timeSeconds = 30; // Reset timer
                                messages.clear();
                                try {
                                    broadcastDayNightCycle();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            // if the stage is voting time, when the timer runs out, switch to night,
                            // and set the timer to 30 seconds.
                            case "VotingTime" -> {
                                stageCycle = "Night";
                                timeSeconds = 30; // Reset timer
                                try {
                                    broadcastDayNightCycle();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }

            }
        }, 0, 1000); // Update every second
    }

    void broadcastDayNightCycle() throws InterruptedException {
        // when cycle becomes day, clear all messages and mafia votes
        if (Objects.equals(stageCycle, "Day")) {
            messages.clear();
            mafiaVoteMap.clear();
            for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
                gameSpace.put("messages", messages);
            }
            // when cycle becomes night, clear all messages and execute votes
        } else if (Objects.equals(stageCycle, "Night")) {
            messages.clear();
            executeVoteMap.clear();
            for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
                gameSpace.put("messages", messages);
            }
        }
        broadcastToAllClients("dayNightCycle", stageCycle);
    }


    // Broadcast the time left in the current stage to all clients
    private void broadcastTimeUpdate(int timeSeconds) {
        // Format time as needed
        String time = String.format("%02d:%02d", timeSeconds / 60, timeSeconds % 60);
        broadcastToAllClients("timeUpdate", time);
    }

    private void broadcastToAllClients(String messageType, String messageContent) {
        try {
            for (String username : identityProvider.getPlayersInLobby()) {
                gameSpace.put(messageType, username, messageContent);
            }
        } catch (InterruptedException e) {
            System.out.println("Error broadcasting message: " + e.getMessage());
        }
    }

    public boolean isRunning() {
        return serverThread.isAlive();
    }

    public String getPlayersInLobby() {
        return identityProvider.getPlayersInLobby().toString();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public List<String> getMessages() {
        return messages;
    }

    public Space getGameSpace() {
        return gameSpace;
    }

    public StatusControl getStatusControl() {
        return statusControl;
    }
}
