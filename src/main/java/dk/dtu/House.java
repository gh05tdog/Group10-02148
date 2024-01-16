package dk.dtu;

import org.jspace.ActualField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

public class House {
    Space houses = new SequentialSpace();

    public House(int noPlayer) throws InterruptedException {
        for (int i = 0; i < noPlayer; i++) {
            houses.put(i, "lock"); // Adding initial locks
        }
    }

    // Attempt to enter house with int no
    public synchronized boolean enterHouse(int no) throws InterruptedException {
        Object[] lock = houses.getp(new ActualField(no), new ActualField("lock"));
        return lock != null;
    }

    // Leave house with int no
    public synchronized void leaveHouse(int no) throws InterruptedException {
        houses.put(no, "lock");
    }

    // Checks if anyone is inside house, does not take the lock
    public synchronized boolean lookInsideHouse(int no) throws InterruptedException {
        Object[] lock = houses.queryp(new ActualField(no), new ActualField("lock"));
        return lock != null;
    }
}
