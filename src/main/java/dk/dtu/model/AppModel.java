package dk.dtu.model;

import dk.dtu.config;
import dk.dtu.controller.AppController;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.IOException;

public class AppModel {
    private static RemoteSpace server;
    private final MessageHandler messageHandler;
    private final LobbyManager lobbyManager;
    private String killed;

    public AppModel() throws IOException {
        server = new RemoteSpace(config.getIp() + "/chat?keep");
        this.messageHandler = new MessageHandler(server);
        this.lobbyManager = new LobbyManager(server);
    }

    public void sendMessage(String clientID, String message, String roomId) throws InterruptedException {
        messageHandler.sendMessage(clientID, message, roomId);
    }

    public void joinLobby(String userName) throws InterruptedException {
        lobbyManager.joinLobby(userName);

    }

    public void startListeningForMessages(TextArea messageAreaLobby) {
        messageHandler.startListeningForMessages(messageAreaLobby);
    }

    public void listenforRoleUpdate(AppController appController, String username) {
        Thread roleThread = new Thread(() -> {
            try {
                while (true) {
                    // Retrieve the user list update intended for this client
                    Object[] response = server.get(new ActualField("roleUpdate"), new ActualField(username), new FormalField(String.class));
                    // Update the user list
                    String role = (String) response[2];
                    System.out.println(username + " has role: " + role);
                    Platform.runLater(() -> appController.appendRoles(role));
                    config.setRole(role);
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
                    Object[] response = server.get(new ActualField("dayNightCycle"), new ActualField(Username), new FormalField(String.class));
                    String messageContent = (String) response[2];
                    Platform.runLater(() -> appController.updateDayNightCycle(messageContent, killed));
                }
            } catch (InterruptedException e) {
                System.out.println("Updates listening thread interrupted");
            } catch (Exception e) {
                System.out.println("Error in updates listening thread: " + e);
            }
        }).start();

    }

    public void startListeningForTimeUpdate(AppController appController, String Username) {
        new Thread(() -> {
            while (true) {
                try {
                    Object[] resp = server.get(new ActualField("timeUpdate"), new ActualField(Username), new FormalField(String.class));
                    String messageContent = (String) resp[2];
                    Platform.runLater(() -> appController.updateTimeLabel(messageContent));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void startListenForKilled(String Username) {
        new Thread(() -> {
            while (true) {
                try {
                    Object[] resp = server.get(new ActualField("mafiaEliminated"), new ActualField(Username), new FormalField(String.class));
                    killed = (String) resp[2];
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }


    public void startListeningForGameStart(Stage currentStage) {
        lobbyManager.startListeningForGameStart(currentStage);
    }

    public void startGame() throws InterruptedException {
        lobbyManager.startGame();
    }

    public void startListeningForUserUpdates(TextArea userListArea, String clientID) {
        lobbyManager.startListeningForUserUpdates(userListArea, clientID);
    }

    public void AttemptAction(String username, String role, String Victim) throws InterruptedException {
        if (config.getHasVoted()) {
            System.out.println("You have already voted");
            return;
        }
        //If the user click on themselves, do nothing
        if (username.equals(Victim)) {
            System.out.println("You cannot vote on yourself");
            return;
        }

        server.put("action", "executeVote", username, Victim);


        switch (role) {
            case "[Mafia]" -> server.put("action", "MafiaVote", username, Victim);
            case "[Snitch]" -> server.put("action", "Snitch", username, Victim);
            case "[Bodyguard]" -> server.put("action", "Bodyguard", username, Victim);
            default -> System.out.println("You are a Citizen");
        }
        config.setHasVoted(true);
    }

    public void startListenForSnitchUpdate(AppController appController,String username){
        new Thread(() -> {
            while (true) {
                try {
                    Object[] response = server.get(new ActualField("snitchMessage"), new ActualField(username),new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                    String snitcherRole = (String) response[2];
                    String victimUsername = (String) response[4];
                    String roleOfVictim = (String) response[3];
                    Platform.runLater(() -> appController.updateSnitchMessage(snitcherRole, victimUsername, roleOfVictim));

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }


    //Listen for the result of the game
    public void startListenForGameResult(AppController appController, String username){
        new Thread(() -> {
            while (true) {
                try {
                    Object[] response = server.get(new ActualField("gameEnd"), new ActualField(username),new FormalField(String.class));
                    String result = (String) response[2];
                    System.out.println(result);
                    Platform.runLater(() -> appController.updateGameResult(result));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
}
