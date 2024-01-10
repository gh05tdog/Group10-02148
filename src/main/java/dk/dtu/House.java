package dk.dtu;

import org.jspace.*;

public class House {
    Space houses = new SequentialSpace();

    public House (int noPlayer) throws InterruptedException {
        for (int i = 0; 0 < noPlayer; i++) {
            houses.put(i,"lock"); // Adding initial locks
        }
    }

    // Attempt to enter house with int no
    public synchronized boolean enterHouse(int no) throws InterruptedException {
        Object[] lock = houses.getp(new ActualField(no),new ActualField("lock"));
        if (lock != null) {
            return true;
        } else {
            return false;
        }
    }

    // Leave house with int no
    public synchronized void leaveHouse(int no) throws InterruptedException {
        houses.put(no,"lock");
    }
}
