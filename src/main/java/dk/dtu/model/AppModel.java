package dk.dtu.model;

import org.jspace.FormalField;
import org.jspace.SequentialSpace;
import org.jspace.Space;

public class AppModel {
    private Space space;

    public AppModel() {
        this.space = new SequentialSpace();
    }

    public void sendMessage(String message) throws InterruptedException {
        space.put(message);
    }

    public String receiveMessage() throws InterruptedException {
        Object[] tuple = space.get(new FormalField(String.class));
        return (String) tuple[0];
    }
}
