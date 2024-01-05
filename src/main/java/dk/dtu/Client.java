package dk.dtu;

import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.util.Scanner;
import java.util.UUID;

public class Client {
    public static void main(String[] args) {
        try {
            RemoteSpace server = new RemoteSpace("tcp://10.209.220.33:9001/chat?keep");
            Scanner scanner = new Scanner(System.in);
            String clientID = UUID.randomUUID().toString(); // Unique ID for each client

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            System.out.print("Enter chat room: ");
            String room = scanner.nextLine();

            // Join the chat room
            server.put("join", room, clientID);

            // Thread to handle incoming messages
            new Thread(() -> {
                try {
                    while (true) {
                        Object[] response = server.get(new ActualField("message"), new ActualField(room), new ActualField(clientID), new FormalField(String.class));
                        System.out.println(response[3]); // Display the message
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();

            // Sending messages
            while (true) {
                String message = scanner.nextLine();
                server.put("message", room, username + ": " + message); // Send message
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}