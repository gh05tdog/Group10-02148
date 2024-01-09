package dk.dtu.controller;

import dk.dtu.App;
import dk.dtu.config;
import dk.dtu.model.AppModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class AppController {
    public TextArea usernameList;
    public Label timerLabel;
    @FXML
    public ImageView background;
    private RemoteSpace server;
    public TextField usernameField;
    public TextField chatroomField;
    @FXML
    private TextField messageField;
    @FXML
    private TextArea messageArea;

    private final AppModel model;

    private Thread messageThread;
    private Thread userThread;
    private Thread listenForPublicMsg;
    private String clientID;

    @FXML
    public ImageView counter;

    private Timeline timeline;
    private Integer timeSeconds = 10;

    public Image moon = new Image("/dk/dtu/view/images/Moon.png");

    public Image sun = new Image("/dk/dtu/view/images/sun.png");

    public Image day = new Image("/dk/dtu/view/images/moonlit_main_day.jpg");

    public Image night = new Image("/dk/dtu/view/images/moonlit_main_night.jpg");

    @FXML
    private AnchorPane settings;

    int managecycle = 0;

    public AppController() throws IOException {
        model = new AppModel();
        this.server = model.getServer();
    }

    @FXML
    private void initialize() {
        this.server = model.getServer();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

    }

    @FXML
    private void handleSendAction() {
        String message = messageField.getText();
        try {
            server.put("message", chatroomField.getText(), usernameField.getText() + ": " + message + "\n"); // Send message
        } catch (InterruptedException e) {
            System.out.println("error:" + e);
        }
        //Clear the message field
        messageField.clear();
    }

    @FXML
    private void handleConnectAction(){
        try {
            // Check if a username is entered
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                System.out.println("Username is required to join the chat room.");
                return;
            }

            // Disconnect from the current room if already connected
            if (clientID != null && !clientID.isEmpty()) {
                server.put("leave", chatroomField.getText(), clientID);
                terminateThreads(); // Terminate existing threads
                Platform.runLater(() -> usernameList.clear()); // Clear the username list
            }

            // Update the clientID with the provided username
            clientID = username;

            // Get the room ID from the chatroomField
            String roomID = chatroomField.getText().trim();
            if (roomID.isEmpty()) {
                System.out.println("Room ID is required to join the chat room.");
                return;
            }

            // Ensure server connection is established
            if (server == null) {
                System.out.println("Server connection is not established.");
                model.setServer(config.getIp());
                server = model.getServer();
            }

            // Join the new chat room
            server.put("join", roomID, clientID);

            // Start threads for handling messages and user updates
            startMessageThread(roomID, clientID);
            startUserThread(roomID, clientID);
            listenForPublicMsg();

        } catch (InterruptedException | IOException e) {
            System.out.println("Error in handleConnectAction: " + e.getMessage());
        }
    }

    private void terminateThreads() {
        if (messageThread != null && messageThread.isAlive()) {
            messageThread.interrupt();
        }
        if (userThread != null && userThread.isAlive()) {
            userThread.interrupt();
        }
        if(listenForPublicMsg != null && listenForPublicMsg.isAlive()){
            listenForPublicMsg.interrupt();
        }
    }



    private void startMessageThread(String roomID, String clientID) {
        messageThread = new Thread(() -> {
            try {
                while (true) {
                    Object[] response = server.get(new ActualField("message"), new ActualField(roomID), new ActualField(clientID), new FormalField(String.class));
                    messageArea.appendText((String) response[3]);
                }
            } catch (Exception e) {
                System.out.println("Messagethread error: " + e);
            }
        });
        messageThread.start();
    }

    private void listenForPublicMsg(){
        messageThread = new Thread(() -> {
            try {
                while (true) {
                    Object[] response = server.get(new ActualField("message"), new ActualField("public"), new ActualField(""), new FormalField(String.class));
                    messageArea.appendText((String) response[3]);
                }
            } catch (Exception e) {
                System.out.println("Message-thread error: " + e);
            }
        });
        messageThread.start();
    }

    private void startUserThread(String roomID, String clientID) {
        userThread = new Thread(() -> {
            try {
                while (true) {
                    Object[] response = server.get(new ActualField("users"), new ActualField(roomID), new ActualField(clientID), new FormalField(String.class));
                    String userList = (String) response[3];
                    Platform.runLater(() -> usernameList.setText(userList));
                }
            } catch (Exception e) {
                System.out.println("User thread: " + e);
            }
        });
        userThread.start();
    }

    private void updateTimer() {

        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        if (timeSeconds > -1) {
            timeSeconds--;
        } else {
            if(managecycle == 0) {
                managecycle = 1;
                counter.setImage(moon);
                background.setImage(night);
                timeSeconds = 10;
            }
            else if(managecycle == 1) {
                managecycle = 0;
                counter.setImage(sun);
                background.setImage(day);
                timeSeconds = 10;
            }
    }
    }
    @FXML
    private void returnToMenu() throws Exception {
        Stage stage = (Stage) settings.getScene().getWindow();
        stage.setScene(App.getScene());

    }
}
