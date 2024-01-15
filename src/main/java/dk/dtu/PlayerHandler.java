package dk.dtu;


import org.jspace.SequentialSpace;
public class PlayerHandler {
    private String username;
    private boolean isActive;
    private int playerID;
    private String role;

    public PlayerHandler(String username, int playerID, SequentialSpace gameSpace) {
        this.username = username;
        this.isActive = true;
        this.playerID = playerID;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getRole() {
        return role;
    }

    public int getPlayerID() {
        return playerID;
    }
}
