package dk.dtu.model;

import dk.dtu.Server;
import dk.dtu.config;
import org.jspace.*;

import java.io.IOException;

public class AppModel {
    private final Space space;
    public RemoteSpace server;
    private Server localServer;

    public AppModel() throws IOException {
        this.space = new SequentialSpace();
        this.server = new RemoteSpace(config.getIp() + "/chat?keep");
        // If you need to connect to a server at initialization, do it here
    }
    public void ChangeSeverIP(String serverIp) throws IOException {
        System.out.println(serverIp + " is the new server IP");
        server = new RemoteSpace(serverIp + "/chat?keep");
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
