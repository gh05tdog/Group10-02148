package dk.dtu;

public class StatusControlTest {
    public static void main(String[] args) {
        try {
            // Define the number of players and their roles
            int noOfPlayers = 5;
            String[] rolelist = {"Villager", "Seer", "Werewolf", "Villager", "Werewolf"};

            // Creates an instance of StatusControl
            StatusControl statusControl = new StatusControl(noOfPlayers, rolelist);

            // CASE 1: Attempted murder with no protection
            // ---------------------------------------------------------------------------------
            System.out.println("CASE 1: Attempt a murder on unprotected player. Should succeed");
            statusControl.attemptMurder(1);
            System.out.println("Player 1 status: Killed - " + statusControl.conductor[1].isKilled());
            System.out.println();
            // ---------------------------------------------------------------------------------

            // CASE 2: Attempted murder with successful protection
            // ---------------------------------------------------------------------------------
            System.out.println("CASE 2: Attempt a murder on protected player after 5 seconds, should not succeed");
            statusControl.protectPlayer(2);
            System.out.println("Player 2 status: Secured - " + statusControl.conductor[2].isSecured());

            try {
                // Wait for 5 seconds
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("5 seconds have passed...");
            System.out.println("Player 2 status: Secured - " + statusControl.conductor[2].isSecured());

            statusControl.attemptMurder(2);
            System.out.println("Player 2 status: Killed - " + statusControl.conductor[2].isKilled());
            System.out.println();
            // ---------------------------------------------------------------------------------

            // CASE 3: Attempted murder with protection that runs out
            // ---------------------------------------------------------------------------------
            System.out.println("CASE 3: Attempt a murder on protected player after 11 seconds, should succeed");
            statusControl.protectPlayer(3);
            System.out.println("Player 3 status: Secured - " + statusControl.conductor[3].isSecured());

            try {
                // Wait for 11 seconds
                Thread.sleep(11000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("11 seconds have passed...");
            System.out.println("Player 3 status: Secured - " + statusControl.conductor[3].isSecured());

            statusControl.attemptMurder(3);
            System.out.println("Player 3 status: Killed - " + statusControl.conductor[3].isKilled());
            // ---------------------------------------------------------------------------------

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

