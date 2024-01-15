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
    private StatusControl statusControl;

    private boolean gameStarted;
    private String stageCycle = "Day"; // Initial state
    private final List<String> messages = new ArrayList<>();
    private int timeSeconds = 30;
    private boolean isTimerRunning = false;
    private final Thread actionThread;
    private final HashMap<String, String> mafiaVoteMap;
    private final HashMap<String, String> executeVoteMap;
    public String[] roleList;
    public String[] nameList;

    private final Set<String> reservedUsernames = new HashSet<>();

    public IdentityProvider identityProvider = new IdentityProvider();

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
        if (reserveUsername(username)) {
            if (!gameStarted && !identityProvider.isPlayerInLobby(username)) {
                gameSpace.put("connected", username);
                identityProvider.addPlayer(username);
                System.out.println("User " + username + " joined the lobby");
                messages.add(username + " joined the lobby");
                broadcastLobbyUpdate();
                releaseUsername(username);
            } else {
                throw new Exception("Game already started or user already in lobby");
            }
        } else {
            throw new Exception("Username already taken or reserved");
        }
    }

    void startGame() throws InterruptedException {
        broadcastToAllClients("startGame", "");
        gameSpace.put("gameStarted");
        assignRolesToPlayers();
        gameStarted = true;
        manageDayNightCycle();
        statusControl = new StatusControl(identityProvider.getNumberOfPlayersInLobby(), nameList, roleList);
        for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
            broadcastRoleUpdate(identityProvider.getPlayersInLobby().toArray()[i].toString(), i);
        }
        System.out.println("Game is starting with players: " + identityProvider.getPlayersInLobby());

    }

    private void assignRolesToPlayers() throws InterruptedException {
        if (!gameStarted) {
            RandomSpace roles = new RandomSpace();
            roleList = new String[identityProvider.getNumberOfPlayersInLobby()];
            nameList = new String[identityProvider.getNumberOfPlayersInLobby()];

            int nrOfMafia = identityProvider.getNumberOfPlayersInLobby()/4;
            for(int i = 0; i < nrOfMafia; i++){
            roles.put("Mafia");
            }
            roles.put("Snitch");
            roles.put("Bodyguard");

             for(int i = 0; i < identityProvider.getNumberOfPlayersInLobby() - nrOfMafia - 2; i++){
            roles.put("Citizen");
             }

            for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
                String role = Arrays.toString(roles.get(new FormalField(String.class)));
                roleList[i] = role;
                nameList[i] = identityProvider.getPlayersInLobby().toArray()[i].toString();
                System.out.println("Player " + nameList[i] + "with ID: " + i + " has role: " + roleList[i]);
            }
            System.out.println("Role list:" + Arrays.toString(roleList));

        } else {
            System.out.println("Game already started");
        }
    }

    public void broadcastRoleUpdate(String username, int playerID) {
        try {
            gameSpace.put("roleUpdate", username, statusControl.getPlayerRole(playerID));
            // System.out.println("Role update sent to: " + username + "with role: " + playerHandlers.get(username).getRole());

        } catch (InterruptedException e) {
            System.out.println("Error broadcasting role update: " + e);
        }
    }

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
        if (!statusControl.conductor[statusControl.getIDFromUserName(yourUsername)].isKilled()) {
            if (Objects.equals(yourUsername, suspect)) {
                System.out.println("Dont vote for yourself, dummy!");
                return;
            } else {
                executeVoteMap.put(yourUsername, suspect);
                System.out.println("Vote received from: " + yourUsername + " on: " + suspect);
            }

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

            int divided = (identityProvider.getNumberOfPlayersInLobby() / 2) + 1;

            if (maxVotes >= divided) {
                System.out.println("The town eliminated: " + mostVotedUser);
                executeVoteCount.clear();
                statusControl.executeSuspect(statusControl.getIDFromUserName(mostVotedUser));
                broadcastToAllClients("mafiaEliminated", mostVotedUser);
                checkForVictory();
                System.out.println(statusControl.conductor[statusControl.getIDFromUserName(mostVotedUser)].isKilled());
            }
        }
    }
    boolean reserveUsername(String username) {
        synchronized (reservedUsernames) {
            if (identityProvider.isPlayerInLobby(username) || reservedUsernames.contains(username)) {
                return false; // Username is already taken or reserved
            }
            reservedUsernames.add(username);
            return true; // Username reserved
        }
    }



    void releaseUsername(String username) {
        synchronized (reservedUsernames) {
            reservedUsernames.remove(username);
        }
    }

    private void bodyguardAction(String yourUsername, String victim) throws InterruptedException {
        if (!statusControl.conductor[statusControl.getIDFromUserName(yourUsername)].isKilled()) {
            System.out.println("Protecting player: " + victim);
            statusControl.protectPlayer(statusControl.getIDFromUserName(victim));
            System.out.println("BODYGUARD: " + statusControl.conductor[statusControl.getIDFromUserName(victim)].isSecured());
        }
    }

    private void snitchAction(String yourUsername, String victim) throws InterruptedException {
        if (!statusControl.conductor[statusControl.getIDFromUserName(yourUsername)].isKilled()) {
            System.out.println("Snitching on player: " + victim);
            System.out.println("SNITCH: " + statusControl.getPlayerRole(statusControl.getIDFromUserName(victim)));
            if (!(statusControl.conductor[statusControl.getIDFromUserName(victim)].isSecured())) {
                broadCastToSnitch(yourUsername, victim, statusControl.getPlayerRole(statusControl.getIDFromUserName(victim)));
            } else {
                System.out.println("Snitching failed");
            }
        }
    }


    private void mafiaVote(String yourUsername, String victim) throws InterruptedException {
        if (!statusControl.conductor[statusControl.getIDFromUserName(yourUsername)].isKilled()) {
            int nrOfMafia = 1;
            System.out.println(identityProvider.getPlayersInLobby());
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
                    statusControl.attemptMurder(statusControl.getIDFromUserName(mostVotedUser));
                    if ((statusControl.conductor[statusControl.getIDFromUserName(mostVotedUser)].isKilled())) {
                        broadcastToAllClients("mafiaEliminated", mostVotedUser);
                        checkForVictory();
                    } else {
                        System.out.println("Mafia kill failed");
                    }

                    System.out.println(statusControl.conductor[statusControl.getIDFromUserName(mostVotedUser)].isKilled());
                    return;
                }

                voteCount.clear();
                if (maxVotes == 1) {
                    // If all values are different, everyone has 1 vote
                    System.out.println("All users have 1 vote.");
                } else {
                    // Otherwise, print the victim with the most votes
                    System.out.println("Victim with the most votes: " + mostVotedUser);
                    statusControl.attemptMurder(statusControl.getIDFromUserName(mostVotedUser));
                    System.out.println(statusControl.conductor[statusControl.getIDFromUserName(mostVotedUser)].isKilled());
                    if (statusControl.conductor[statusControl.getIDFromUserName(mostVotedUser)].isKilled()) {
                        broadcastToAllClients("mafiaEliminated", mostVotedUser);
                        checkForVictory();
                    } else {
                        System.out.println("Mafia kill failed");
                    }
                }
            }

        }
    }

    public void broadCastToSnitch(String username, String victimRole, String victimUsername) throws InterruptedException {
        if (statusControl.getPlayerRole(statusControl.getIDFromUserName(username)).contains("Snitch")) {
            System.out.println("Sending snitch message to: " + username);
            gameSpace.put("snitchMessage", username, statusControl.getPlayerRole(statusControl.getIDFromUserName(username)), victimUsername, victimRole);
        }

    }

    private void endGame(String message) {
        //Stop threads
        isTimerRunning = false;
        broadcastToAllClients("gameEnd", message);
    }

    private void checkForVictory() {
        int mafiaCount = 0;
        int nonMafiaCount = 0;

        for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
            if (statusControl.conductor[i].isKilled()) continue; // Skip dead players

            if (statusControl.conductor[i].getRole().contains("Mafia")) {
                mafiaCount++;
            } else {
                nonMafiaCount++;
            }
        }

        if (mafiaCount >= nonMafiaCount) {
            endGame("Mafia wins!");
            System.out.println("Mafia wins!");
        } else if (mafiaCount == 0) {
            endGame("Citizens win!");
            System.out.println("Citizens win!");
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

        isTimerRunning = true;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (isTimerRunning) {
                    if (timeSeconds > 0) {
                        timeSeconds--;
                        broadcastTimeUpdate(timeSeconds);
                    } else {
                        switch (stageCycle) {
                            case "Day" -> {
                                stageCycle = "VotingTime";
                                System.out.println(stageCycle);
                                timeSeconds = 30; // Reset timer
                                try {
                                    broadcastDayNightCycle();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case "Night" -> {
                                stageCycle = "Day";
                                timeSeconds = 30; // Reset timer
                                System.out.println(stageCycle);
                                try {
                                    broadcastDayNightCycle();
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            case "VotingTime" -> {
                                stageCycle = "Night";
                                timeSeconds = 30; // Reset timer
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

            }
        }, 0, 1000); // Update every second
    }

    void broadcastDayNightCycle() throws InterruptedException {
        if (Objects.equals(stageCycle, "Day")) {
            messages.clear();
            mafiaVoteMap.clear();
            for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
                gameSpace.put("messages", messages);
            }
        } else if (Objects.equals(stageCycle, "Night")) {
            messages.clear();
            executeVoteMap.clear();
            for (int i = 0; i < identityProvider.getNumberOfPlayersInLobby(); i++) {
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
            for (String username : identityProvider.getPlayersInLobby()) {
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
        return identityProvider.getPlayersInLobby().toString();
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public List<String> getMessages() {
        return messages;
    }

}
