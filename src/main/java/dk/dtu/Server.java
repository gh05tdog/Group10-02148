package dk.dtu;

import org.jspace.*;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server implements Runnable {
    private SpaceRepository repository;
    private SequentialSpace chatSpace;
    private String serverIp;
    private Thread serverThread;

    public Server() throws UnknownHostException {
        serverIp = InetAddress.getLocalHost().getHostAddress();
        repository = new SpaceRepository();
        chatSpace = new SequentialSpace();
        serverThread = new Thread(this);
    }

    public void startServer() {
        serverThread.start();
    }

    @Override
    public void run() {
        try {
            repository.add("chat", chatSpace);
            String gateUri = "tcp://" + serverIp + ":9001/?keep";
            repository.addGate(gateUri);

            System.out.println("Chat server running at " + gateUri);

            Map<String, Set<String>> roomClients = new HashMap<>();

            while (!Thread.currentThread().isInterrupted()) {
                Object[] request = chatSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                String action = (String) request[0];
                String room = (String) request[1];
                String content = (String) request[2];

                roomClients.putIfAbsent(room, new HashSet<>());

                handleRequest(action, room, content, roomClients);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Server thread interrupted");
        } catch (Exception e) {
            System.out.println("Server error: " + e.toString());
        }
    }

    private void handleRequest(String action, String room, String content, Map<String, Set<String>> roomClients) throws InterruptedException {
        switch (action) {
            case "join" -> {
                roomClients.get(room).add(content); // content is the client ID here
                System.out.println("Client " + content + " joined room " + room);
            }
            case "leave" -> roomClients.get(room).remove(content); // content is the client ID here
            case "message" -> {
                for (String clientID : roomClients.get(room)) {
                    chatSpace.put("message", room, clientID, content); // Broadcast message
                    System.out.println("Broadcasting message to " + clientID + " in room " + room);
                    System.out.println("Message: " + content);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            new Server().startServer();
        } catch (UnknownHostException e) {
            System.out.println("Could not determine local host IP");
            System.out.println(e.toString());
        }
    }
}
