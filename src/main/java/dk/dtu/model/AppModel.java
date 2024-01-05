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
    }


    public void setServer(String ip) throws IOException {
        this.server = new RemoteSpace(ip + "/chat?keep");
    }

    public RemoteSpace getServer() {
        return this.server;
    }
}
