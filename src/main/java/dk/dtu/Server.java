package dk.dtu;

import org.jspace.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {
    private SpaceRepository repository;
    private SequentialSpace chatSpace;
    private String serverIp;

    public Server(String ip) {
        serverIp = ip;
        repository = new SpaceRepository();
        chatSpace = new SequentialSpace();
    }

    public void startServer() throws InterruptedException {
        try {
            repository.add("chat", chatSpace);
            String gateUri = serverIp + "/?keep";
            repository.addGate(gateUri);

            System.out.println("Chat server running at " + gateUri);

            Map<String, Set<String>> roomClients = new HashMap<>();

            while (true) {
                Object[] request = chatSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                String action = (String) request[0];
                String room = (String) request[1];
                String content = (String) request[2];

                roomClients.putIfAbsent(room, new HashSet<>());

                handleRequest(action, room, content, roomClients);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRequest(String action, String room, String content, Map<String, Set<String>> roomClients) throws InterruptedException {
        switch (action) {
            case "join":
                roomClients.get(room).add(content); // content is the client ID here
                break;
            case "leave":
                roomClients.get(room).remove(content); // content is the client ID here
                break;
            case "message":
                for (String clientID : roomClients.get(room)) {
                    chatSpace.put("message", room, clientID, content); // Broadcast message
                    System.out.println("Broadcasting message to " + clientID + " in room " + room);
                    System.out.println("Message: " + content);
                }
                break;
        }
    }

    public static void main(String[] args) {
        try {
            new Server("tcp://localhost:9001").startServer();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
