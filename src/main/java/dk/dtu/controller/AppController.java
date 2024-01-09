package dk.dtu.controller;

import dk.dtu.config;
import dk.dtu.model.AppModel;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.Objects;

public class AppController {
    public TextArea usernameList; // LOBBY
    public Label timerLabel;
    @FXML
    public ImageView background;
    public TextField chatroomField;
    public Button StartGameButton;
    public TextArea messageAreaLobby;
    private RemoteSpace server;
    public TextField usernameField;

    @FXML
    private TextField messageField;

    @FXML
    private TextArea messageArea; //Lobby


    private final AppModel model;
    private String clientID;

    @FXML
    public ImageView counter;

    private Timeline timeline;
    private Integer timeSeconds = 10;

    public Image moon = new Image("/dk/dtu/view/images/Moon.png");

    public Image sun = new Image("/dk/dtu/view/images/sun.png");

    public Image day = new Image("/dk/dtu/view/images/moonlit_main_day.jpg");

    public Image night = new Image("/dk/dtu/view/images/moonlit_main_night.jpg");

    int managecycle = 0;

    public AppController() throws IOException {
        model = new AppModel();
    }
    @FXML
    private void initialize() {
        /*
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();*/
    }

    @FXML
    private void handleSendAction() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                System.out.println("Sending message: " + message);
                model.sendMessage(config.getUsername(), message, "lobby");
            } catch (Exception e) {
                System.out.println("Error sending message: " + e);
            }
            messageField.clear();
        }
    }

    @FXML
    private void handleConnectAction() throws InterruptedException {
        String username = usernameField.getText().trim();
        config.setUsername(username);


        if (username.isEmpty()) {
            System.out.println("Username is required to join the lobby.");
            return;
        }
        if (clientID != null && !clientID.isEmpty()) {
            model.leaveLobby(clientID);
        }

        model.joinLobby(config.getUsername());
        model.startListeningForMessages(messageArea, messageAreaLobby);
    }

    private void updateTimer() {
        int minutes = timeSeconds / 60;
        int seconds = timeSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

        if (timeSeconds > 0) {
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

    public void StartGameAction(ActionEvent event) throws IOException {
        //TODO: DO STUFF

        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/App_view.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
        model.startListeningForMessages(messageArea, messageAreaLobby);
    }

}
