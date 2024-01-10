package dk.dtu;


import org.jspace.SequentialSpace;
public class PlayerHandler implements Runnable {
    private String username;
    private SequentialSpace gameSpace;
    private boolean isActive;
    private int playerID;

    public PlayerHandler(String username, int playerID, SequentialSpace gameSpace) {
        this.username = username;
        this.gameSpace = gameSpace;
        this.isActive = true;
        this.playerID = playerID;
    }

    @Override
    public void run() {
        try {
            while (isActive) {

                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            System.out.println("Player handler for " + username + " interrupted.");
        }
    }

    public void stop() {
        isActive = false;
    }
}
