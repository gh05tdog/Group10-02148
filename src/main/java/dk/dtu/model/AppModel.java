package dk.dtu.model;

import org.jspace.FormalField;
import org.jspace.RemoteSpace;
import org.jspace.SequentialSpace;
import org.jspace.Space;

import java.io.IOException;

public class AppModel {
    private Space space;
    private RemoteSpace server;

    public AppModel() throws IOException {
        this.space = new SequentialSpace();
        this.server = new RemoteSpace("tcp://10.209.220.33:9001/chat?keep");
    }

    public void sendMessage(String message) throws InterruptedException {
        space.put(message);
    }

    public RemoteSpace getServer() throws IOException {
        return server;
    }



    public String receiveMessage() throws InterruptedException {
        Object[] tuple = space.get(new FormalField(String.class));
        return (String) tuple[0];
    }
}
