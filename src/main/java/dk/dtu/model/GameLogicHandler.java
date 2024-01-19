package dk.dtu.model;

import dk.dtu.config;
import dk.dtu.controller.AppController;
import javafx.application.Platform;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

public class GameLogicHandler {
    private final RemoteSpace server;

    GameLogicHandler(RemoteSpace server) {
        this.server = server;
    }

    public void AttemptAction(String username, String role, String Victim) throws InterruptedException {
        if (config.getHasVoted()) {
            return;
        }

        server.put("action", "executeVote", username, Victim);
        System.out.println("Action sent");

        switch (role) {
            case "[Mafia]" -> server.put("action", "MafiaVote", username, Victim);
            case "[Snitch]" -> server.put("action", "Snitch", username, Victim);
            case "[Bodyguard]" -> server.put("action", "Bodyguard", username, Victim);
        }
        config.setHasVoted(true);
    }

    public void startListenForSnitchUpdate(AppController appController, String username) {
        new Thread(() -> {
            while (true) {
                try {
                    Object[] response = server.get(new ActualField("snitchMessage"), new ActualField(username), new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
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

    public void startListenForGameResult(AppController appController, String username) {
        new Thread(() -> {
            while (true) {
                try {
                    Object[] response = server.get(new ActualField("gameEnd"), new ActualField(username), new FormalField(String.class));
                    String result = (String) response[2];
                    Platform.runLater(() -> appController.updateGameResult(result));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void listenForRoleUpdate(AppController appController, String username) {
        Thread roleThread = new Thread(() -> {
            try {
                while (true) {
                    // Retrieve the user list update intended for this client
                    Object[] response = server.get(new ActualField("roleUpdate"), new ActualField(username), new FormalField(String.class));
                    // Update the user list
                    String role = (String) response[2];
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


}
