package dk.dtu;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusControlTest {
    private StatusControl statusControl;

    @BeforeEach
    void setUp() throws InterruptedException {
        int noOfPlayers = 5;
        String[] rolelist = {"Villager", "Seer", "Werewolf", "Villager", "Werewolf"};

        statusControl = new StatusControl(noOfPlayers, rolelist);
    }

    @Test
    void testMurderWithoutProtection() throws InterruptedException {

        statusControl.attemptMurder(1);

        assertFalse(statusControl.conductor[1].isSecured());
        assertTrue(statusControl.conductor[1].isKilled());
    }

    @Test
    void testMurderWithProtection() throws InterruptedException {

        statusControl.protectPlayer(2);

        assertFalse(statusControl.conductor[2].isKilled());

        // Wait for 5 seconds
        Thread.sleep(5000);

        assertTrue(statusControl.conductor[2].isSecured());

        statusControl.attemptMurder(2);

        assertFalse(statusControl.conductor[2].isKilled());
    }

    @Test
    void testMurderAfterProtectionExpires() throws InterruptedException {

        statusControl.protectPlayer(3);

        assertFalse(statusControl.conductor[3].isKilled());

        // Wait for 11 seconds
        Thread.sleep(11000);

        assertFalse(statusControl.conductor[3].isSecured());

        statusControl.attemptMurder(3);

        assertTrue(statusControl.conductor[3].isKilled());
    }
}
