package dk.dtu;

import org.jspace.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Server {
    public static void main(String[] args) {
        try {
            SpaceRepository repository = new SpaceRepository();
            SequentialSpace chatSpace = new SequentialSpace();
            repository.add("chat", chatSpace);
            repository.addGate("tcp://10.209.220.33:9001/?keep");
            System.out.println("Chat server running...");

            Map<String, Set<String>> roomClients = new HashMap<>();

            while (true) {
                Object[] request = chatSpace.get(new FormalField(String.class), new FormalField(String.class), new FormalField(String.class));
                String action = (String) request[0];
                String room = (String) request[1];
                String content = (String) request[2];

                roomClients.putIfAbsent(room, new HashSet<>());

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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
