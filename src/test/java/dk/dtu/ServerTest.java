package dk.dtu;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

public class ServerTest {

    private Server server;


    /**
     * Set up the server before each test
     */
    @BeforeEach
    void setUp() throws InterruptedException, UnknownHostException {
        server = new Server();
        server.startServer();
        // Wait for the server to start
        Thread.sleep(1000);
    }

    @AfterEach
    void serverStop() {
        server.stopServer();
    }

    @Test
    void testServerInitialization() {
        assertNotNull(server);
        assertTrue(server.isRunning());
    }

    /**
     * Test that the server can join a lobby
     * @throws Exception if the server cannot join the lobby
     */
    @Test
    void testJoiningLobby() throws Exception {
        server.handleJoinLobby("player1");
        assertTrue(server.getPlayersInLobby().contains("player1"));
    }


    /**
     * Test that the server can start a game
     * @throws Exception if the server cannot start the game
     */
    @Test
    void testStartGame() throws Exception {
        server.handleJoinLobby("player1");
        server.startGame();
        assertTrue(server.isGameStarted());
    }

    /**
     * Test that the server can send messages
     * @throws Exception if the server cannot send messages
     */

    @Test
    void testMessageSending() throws Exception {
        server.handleJoinLobby("player1");
        server.handleJoinLobby("player2");
        server.handleJoinLobby("player3");
        //Send message from player 1
        server.handleMessage("player1", "Hello");
        assertEquals("player1 joined the lobby", server.getMessages().get(0));
        assertEquals("player1: Hello", server.getMessages().get(3));
    }

    /**
     * Test that the server ads users to the lobby one at a time
     */

    @Test
    void testAddUserOneAtATime() {

        // Thread A: Try to join the lobby
        new Thread(() -> {
            try {
                server.getGameSpace().get(new ActualField("lock"));
                server.getGameSpace().put("usernameCheck", "player1");
                Object[] response =  server.getGameSpace().get(new ActualField("usernameCheck"), new FormalField(Boolean.class));
                assertTrue((Boolean) response[1]);
                server.getGameSpace().put("joinLobby", "player1");
                //Ensure that the server has time to add the user to the lobby
                Thread.sleep(1000);
                assertTrue(server.getPlayersInLobby().contains("player1"));
                server.getGameSpace().put("lock");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


        // Thread B: Try to add a user that is already in the lobby
        new Thread(() -> {
            try {
                server.getGameSpace().get(new ActualField("lock"));
                server.getGameSpace().put("usernameCheck", "player1");
                Object[] response =  server.getGameSpace().get(new ActualField("usernameCheck"), new FormalField(Boolean.class));
                assertFalse((Boolean) response[1]);
                server.getGameSpace().put("lock");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        // Thread C: Try to add a user that is not in the lobby
        new Thread(() -> {
            try {
                server.getGameSpace().get(new ActualField("lock"));
                server.getGameSpace().put("usernameCheck", "player2");
                Object[] response =  server.getGameSpace().get(new ActualField("usernameCheck"), new FormalField(Boolean.class));
                assertTrue((Boolean) response[1]);
                server.getGameSpace().put("joinLobby", "player2");
                //Ensure that the server has time to add the user to the lobby
                Thread.sleep(1000);
                assertTrue(server.getPlayersInLobby().contains("player2"));
                server.getGameSpace().put("lock");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Test that the server can assign roles to players
     * @throws Exception if the server cannot assign roles to players
     */

    @Test
    void testAssignRolesToPlayers() throws Exception {
        for (int i = 1; i <= 4; i++) { // Add 4 players for this test
            server.handleJoinLobby("player" + i);
        }
        server.startGame();
        assertNotNull(server.roleList);
        assertEquals(4, server.roleList.length); // Check if roles are assigned to all players
    }


    /**
    * Test that the server can handle voteExecution
     */


    @Test
    void testExecuteVote() throws Exception {
        //Ads 3 players to the lobby
        server.handleJoinLobby("player1");
        server.handleJoinLobby("player2");
        server.handleJoinLobby("player3");

        server.startGame(); // Ensure the game is started

        // Simulate a voting scenario
        server.executeVote("player1", "player2");
        server.executeVote("player2", "player3");
        server.executeVote("player3", "player2");

        // Verify the expected outcome of the vote:
        // Player2 should be killed because she got 2/3 votes
        assertTrue(server.getStatusControl().conductor[server.getStatusControl().getIDFromUserName("player2")].isKilled()); // Assuming such a method exists
    }
}
