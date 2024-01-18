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
    private final GameLogicHandler gameLogicHandler;
    private String killed;

    public AppModel() throws IOException {
        server = new RemoteSpace(config.getIp() + "/chat?keep");
        this.messageHandler = new MessageHandler(server);
        this.lobbyManager = new LobbyManager(server);
        this.gameLogicHandler = new GameLogicHandler(server);
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

    public void startListeningForTimeUpdate(AppController appController, String Username) {
        gameLogicHandler.startListeningForTimeUpdate(appController, Username);
    }


    public void startListeningForGameStart(Stage currentStage) {
        lobbyManager.startListeningForGameStart(currentStage);
    }

    public void startGame() throws InterruptedException {
        server.put("startGame", "game_identifier");
        System.out.println("Game is starting");
    }

    public void startListeningForUserUpdates(TextArea userListArea, String clientID) {
        lobbyManager.startListeningForUserUpdates(userListArea, clientID);
    }


    public void AttemptAction(String username, String role, String Victim) throws InterruptedException {
        gameLogicHandler.AttemptAction(username, role, Victim);
    }

    public void startListenForSnitchUpdate(AppController appController, String username) {
        gameLogicHandler.startListenForSnitchUpdate(appController, username);
    }

    public void startListenForGameResult(AppController appController, String username) {
        gameLogicHandler.startListenForGameResult(appController, username);
    }

    public void listenForRoleUpdate(AppController appController, String username) {
        gameLogicHandler.listenForRoleUpdate(appController, username);
    }
}
