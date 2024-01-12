package dk.dtu;

import org.jspace.ActualField;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Creates a thread which handles the players statuses and actions
class Conductor extends Thread {
    int no;                         // Player number
    String role;                    // Player role
    House house;                    // House control
    boolean killed = false;         // Used to determine if player is still alive

    // Not sure if necessary as we are using locks, keeping in case we need the value for something else, like a seer
    boolean secured = false;        // Used to determine if player is currently being protected

    public Conductor(int no, String role) {
        this.no = no;
        //this.name = name;
        this.role = role;
        //this.col = col;
    }

    void murderAttempt() {
        if (!secured) { //If we manage to properly utilise locks, not sure that this check would become necessary
            killed = true;
            // Do we even want to interrupt threads?
            // interrupt();
        }
    }

    void protectPlayer() {
        secured = true;
    }

    void stopProtectingPlayer() {
        secured = false;
    }

    String getRole() {
        return role;
    }


    boolean isSecured() {
        return secured;
    }

    boolean isKilled() {
        return killed;
    }

    /*
    public void run() {
        while (!killed) {
        }

    }
     */
}

public class StatusControl {
    Conductor[] conductor;      // Status controllers
    House houses;               // Houses
    int noOfPlayers;            // Number of players

    public StatusControl(int noOfPlayers, String[] rolelist) throws InterruptedException {
        this.noOfPlayers = noOfPlayers;
        conductor = new Conductor[noOfPlayers];
        houses = new House(noOfPlayers);

        for (int i = 0; i < noOfPlayers; i++) {
            conductor[i] = new Conductor(i, rolelist[i]);
            conductor[i].start();
        }

    }

    public void attemptMurder(int victim) throws InterruptedException {
        if (houses.enterHouse(victim)) { // Something only happens if able to enter the house
            conductor[victim].murderAttempt();
            houses.leaveHouse(victim);
        }
    }

    public void protectPlayer(int player) throws InterruptedException {
        if (houses.enterHouse(player)) { // Something only happens if able to enter the house
            conductor[player].protectPlayer();

            // Use a ScheduledExecutorService to schedule the stopProtectingPlayer action after 10 seconds
            ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.schedule(() -> {
                conductor[player].stopProtectingPlayer();
                try {
                    houses.leaveHouse(player);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, 10, TimeUnit.SECONDS);

            // Shutdown the executor service to stop it when it's no longer needed
            executorService.shutdown();
        }
    }

    public String getPlayerRole(int player) throws InterruptedException {
        if (houses.lookInsideHouse(player)) {
            return conductor[player].getRole();
        } else {
            return "[REDACTED]";
        }
    }
}
