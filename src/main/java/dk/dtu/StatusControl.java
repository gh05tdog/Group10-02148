package dk.dtu;

import org.jspace.ActualField;

//import java.awt.Color;

// Creates a thread which handles the players statuses and actions
class Conductor extends Thread {
    int no;                         // Player number
    //String name;                    // Name of the player
    String role;                    // Player role
    //Color col;                      // Player color
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

    synchronized void murderAttempt() {
        if (!secured) { //If we manage to properly utilise locks, not sure that this check would become necessary
            killed = true;
            // Do we even want to interrupt threads?
            // interrupt();
        }
    }

    synchronized void protectPlayer() {
        secured = true;
    }

    synchronized void stopProtectingPlayer() {
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
        houses = new House(noOfPlayers);

        for (int i = 0; i < noOfPlayers; i++) {
            conductor[i] = new Conductor(i,rolelist[i]);
            conductor[i].start();
        }

    }

    public void attemptMurder(int victim) throws InterruptedException {
        if (houses.enterHouse(victim)) { // Something only happens if able to enter the house
            conductor[victim].murderAttempt();
            // Is there a need for the thread to sleep? I imagine that we leave the house immediately after killing?
            // Thread.sleep(30000);
            houses.leaveHouse(victim);
        }
    }

    public void protectPlayer(int player) throws InterruptedException {
        if (houses.enterHouse(player)) { // Something only happens if able to enter the house
            conductor[player].protectPlayer();
            // Sleep for 30 seconds, can be changed depending on what we think
            Thread.sleep(30000);
            conductor[player].stopProtectingPlayer();
            houses.leaveHouse(player);
        }
    }
}
