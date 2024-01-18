package dk.dtu;

import java.util.HashSet;

public class IdentityProvider {
    //This class is used to add, remove, get and check if a player is in the lobby

    private final HashSet<String> playersInLobby;

    public IdentityProvider() {
        this.playersInLobby = new HashSet<>();
    }

    public void addPlayer(String username) {
        playersInLobby.add(username);
    }

    public boolean isPlayerInLobby(String username) {
        return playersInLobby.contains(username);
    }

    public HashSet<String> getPlayersInLobby() {
        return playersInLobby;
    }

    public int getNumberOfPlayersInLobby() {
        return playersInLobby.size();
    }

    public void removePlayer(String username) {
        playersInLobby.remove(username);
    }
}
