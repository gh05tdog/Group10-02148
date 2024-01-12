package dk.dtu;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class StatusControlTest {
    private StatusControl statusControl;

    private ScheduledExecutorService executorService;

    @BeforeEach
    void setUp() throws InterruptedException {
        int noOfPlayers = 5;
        String[] rolelist = {"Citizen", "Snitch", "Bodyguard", "Mafia", "Mafia"};

        statusControl = new StatusControl(noOfPlayers, rolelist);

        executorService = Executors.newScheduledThreadPool(1);
    }

    /**
    An unprotected player should be killed when attemptMurder is called
     */
    @Test
    void testMurderWithoutProtection() throws InterruptedException {
        assertFalse(statusControl.conductor[1].isSecured());
        statusControl.attemptMurder(1);
        assertTrue(statusControl.conductor[1].isKilled());
    }

    /**
     A protected player should stay alive when attemptMurder is called
     */
    @Test
    void testMurderWithProtection() throws InterruptedException {
        assertFalse(statusControl.conductor[2].isSecured());
        statusControl.protectPlayer(2);
        assertTrue(statusControl.conductor[2].isSecured());

        Thread.sleep(5000);

        assertTrue(statusControl.conductor[2].isSecured());
        statusControl.attemptMurder(2);
        assertFalse(statusControl.conductor[2].isKilled());
    }

    /**
     If the protection experies, the player becomes unprotected and can be killed again
     */
    @Test
    void testMurderAfterProtectionExpires() throws InterruptedException {
        assertFalse(statusControl.conductor[3].isSecured());
        statusControl.protectPlayer(3);
        assertTrue(statusControl.conductor[3].isSecured());

        executorService.schedule(() -> {
            assertFalse(statusControl.conductor[3].isSecured());
            try {
                statusControl.attemptMurder(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertTrue(statusControl.conductor[3].isKilled());
        }, 11, TimeUnit.SECONDS);

    }

    /**
     The duration of which a player is protected should not be extended
     if .protectPlayer is called whilst already protected
     */
    @Test
    void testDoubleProtectionNotPossible() throws InterruptedException {
        assertFalse(statusControl.conductor[4].isSecured());
        statusControl.protectPlayer(4);
        assertTrue(statusControl.conductor[4].isSecured());

        Thread.sleep(5000);

        statusControl.protectPlayer(4);
        assertTrue(statusControl.conductor[4].isSecured());

        Thread.sleep(6000);

        assertFalse(statusControl.conductor[4].isSecured());
    }

    /**
     Multiple people can be protected at once
     */
    @Test
    void testMultipleProtectedPeople() throws InterruptedException {
        assertFalse(statusControl.conductor[1].isSecured());
        assertFalse(statusControl.conductor[2].isSecured());
        assertFalse(statusControl.conductor[2].isSecured());
        statusControl.protectPlayer(1);
        statusControl.protectPlayer(2);
        statusControl.protectPlayer(3);
        assertTrue(statusControl.conductor[1].isSecured());
        assertTrue(statusControl.conductor[2].isSecured());
        assertTrue(statusControl.conductor[3].isSecured());
    }

    /**
     If nobody is inside a player's house, their role can be revealed
     */
    @Test
    void testSnitchOnUnprotectedPlayer() throws InterruptedException {
        assertTrue(statusControl.houses.lookInsideHouse(4));
        //assertEquals(statusControl.getPlayerRole(4),"Mafia");
        assertEquals(statusControl.getPlayerRole(4),statusControl.conductor[4].getRole());
    }

    /**
     If someone is protected or someone is inside the house, their role will not be revealed
     */
    @Test
    void testSnitchOnProtectedPlayer() throws InterruptedException {
        statusControl.protectPlayer(4);
        assertFalse(statusControl.houses.lookInsideHouse(4));
        assertNotEquals(statusControl.getPlayerRole(4),statusControl.conductor[4].getRole());
    }

    /**
     If the protection expires (or someone leaves the house), a role can be revealed again
     */
    @Test
    void testSnitchAfterProtectionExpires() throws InterruptedException {
        statusControl.protectPlayer(4);
        assertFalse(statusControl.houses.lookInsideHouse(4));
        assertNotEquals(statusControl.getPlayerRole(4),statusControl.conductor[4].getRole());
        Thread.sleep(11000);
        assertTrue(statusControl.houses.lookInsideHouse(4));
        assertEquals(statusControl.getPlayerRole(4),statusControl.conductor[4].getRole());
    }

    /**
     Whilst snitching on someone, they can still be killed
     i.e. snitching does not protect a player
     */
    @Test
    void testKillAfterSnitch() throws InterruptedException {
        assertFalse(statusControl.conductor[2].isKilled());
        statusControl.getPlayerRole(2);
        statusControl.attemptMurder(2);
        assertTrue(statusControl.conductor[2].isKilled());

    }


}
