package dk.dtu;

import javafx.stage.Stage;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class config {
    public static String SERVER_IP;
    private static String Username = null;

    private static Boolean lobbyLeader = false;

    private static String role;
    private static String userList;

    private static Boolean hasVoted = false;


    static {
        try {
            SERVER_IP = "tcp://"+ InetAddress.getLocalHost().getHostAddress() + ":9001/game?keep";

        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public static Boolean getLobbyLeader() {
        return lobbyLeader;
    }

    public static void setLobbyLeader(Boolean lobbyLeaderBool) {
        lobbyLeader = lobbyLeaderBool;
    }

    public static void setIp(String ip) {
        SERVER_IP = "tcp://" + ip + "/game?keep";
    }

    public static String getIp() {
        return SERVER_IP;
    }

    public static void setUsername(String username) {
        Username = username;
    }

    public static String getUsername() {
        return Username;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String newRole) {
        role = newRole;
    }


    public static void setUserList(String currentUserList) {
        userList = currentUserList;
    }
    public static String getUserList() {
        return userList;
    }

    public static Boolean getHasVoted() {
        return hasVoted;
    }

    public static void setHasVoted(Boolean hasVotedUpdated) {
        hasVoted = hasVotedUpdated;
    }
}
