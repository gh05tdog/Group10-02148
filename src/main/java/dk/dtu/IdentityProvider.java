package dk.dtu;

import java.util.HashSet;
import java.util.Set;

public class IdentityProvider {
    private final Set<String> playersInLobby;


    public IdentityProvider() {
        this.playersInLobby = new HashSet<>();
    }

    public void addPlayer(String username) {
        playersInLobby.add(username);
    }

    public boolean isPlayerInLobby(String username) {
        return playersInLobby.contains(username);
    }

    public Set<String> getPlayersInLobby() {
        return playersInLobby;
    }

    public int getNumberOfPlayersInLobby() {
        return playersInLobby.size();
    }

    public void removePlayer(String username) {
        playersInLobby.remove(username);
    }
}
