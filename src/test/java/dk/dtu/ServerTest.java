package dk.dtu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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

        // Initialize the executor service
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    }

    @AfterEach
    void tearDown() {
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


    @Test
    void testStartGame() throws Exception {
        server.handleJoinLobby("player1");
        server.startGame();
        assertTrue(server.isGameStarted());
    }

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

}
