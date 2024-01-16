package dk.dtu;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Creates a thread which handles the players statuses and actions
class Conductor extends Thread {
    int no;                         // Player number
    String userName;                // Player name
    String role;                    // Player role
    boolean killed = false;         // Used to determine if player is still alive
    boolean secured = false;        // Used to determine if player is currently being protected

    public Conductor(int no, String userName, String role) {
        this.no = no;
        this.userName = userName;
        this.role = role;
    }

    void murder() {
        killed = true;
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

    String getUserName() {
        return userName;
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
    String[] nameList;          // List of all player's names

    public StatusControl(int noOfPlayers, String[] nameList, String[] rolelist) throws InterruptedException {
        this.noOfPlayers = noOfPlayers;
        conductor = new Conductor[noOfPlayers];
        houses = new House(noOfPlayers);
        this.nameList = nameList;

        for (int i = 0; i < noOfPlayers; i++) {
            conductor[i] = new Conductor(i, nameList[i], rolelist[i]);
            conductor[i].start();
        }
    }

    public int getIDFromUserName(String userName) {
        for (int i = 0; i < noOfPlayers; i++) {
            if (Objects.equals(nameList[i], userName)) {
                return i;
            }
        }

        return -1;
    }

    public void attemptMurder(int victim) throws InterruptedException {
        if (houses.enterHouse(victim)) { // Something only happens if able to enter the house
            conductor[victim].murder();
            houses.leaveHouse(victim);
        }
    }

    public void executeSuspect(int suspect) throws InterruptedException {
        conductor[suspect].murder();
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

    public String getPlayerName(int player) {
        return conductor[player].getUserName();
    }


    /*
    public List<String[]> getAlivePlayers () {
        List<String[]> alivePlayers = new ArrayList<>();

        for (int i = 0; i < noOfPlayers; i++) {
            if (!conductor[i].isKilled()) {
                alivePlayers.add(new String[] {i, conductor[i].getRole()} );
            }

        }
        return alivePlayers;
    }

     */
}
