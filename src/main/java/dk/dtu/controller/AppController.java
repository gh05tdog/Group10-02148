package dk.dtu.controller;

import dk.dtu.App;
import dk.dtu.config;
import dk.dtu.model.AppModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.jspace.*;
import java.io.IOException;
import java.util.Objects;
import org.jspace.RandomSpace;
import org.jspace.SequentialSpace;
import org.jspace.Tuple;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class AppController {
    public ImageView background;

    public TextField messageField;
    public TextArea messageArea;
    public TextArea usernameList;
    public ImageView counter;
    public Label timerLabel;

    public AnchorPane settings;

    public Space testSpace;

    private final AppModel model;

    public Scene scene;


    public Image moon = new Image("/dk/dtu/view/images/Moon.png");

    public Image sun = new Image("/dk/dtu/view/images/sun.png");

    public Image day = new Image("/dk/dtu/view/images/moonlit_main_day.jpg");

    public Image night = new Image("/dk/dtu/view/images/moonlit_main_night.jpg");
    @FXML
    public TextArea roleBox;

    public AppController() throws IOException {
        model = new AppModel();
    }

    @FXML
    private void initialize() throws IOException {
//        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDayNightCycle("day")));
//        timeline.setCycleCount(Timeline.INDEFINITE);
//        timeline.play();
        model.listenforRoleUpdate(this, config.getUsername());
        model.startListeningForMessages(messageArea);
        model.startListeningForDayNightCycle(this, config.getUsername());
        model.startListeningForUserUpdates(new LobbyController(), usernameList, config.getUsername());
        System.out.println(config.getUsername());

    }

    public void handleSendAction() {
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

    public void updateDayNightCycle(String state) {
        System.out.println("Received state");
        Platform.runLater(() -> {
            if ("day".equals(state)) {
                counter.setImage(sun);
                background.setImage(day);
            } else if ("night".equals(state)) {
                counter.setImage(moon);
                background.setImage(night);
            }
        });
    }

    public void updateTimeLabel(String time) {
        Platform.runLater(() -> timerLabel.setText(time));
    }

    @FXML
    private void returnToMenu() throws Exception {
        Stage stage = (Stage) settings.getScene().getWindow();
        stage.setScene(App.getStartScene());

    }

    public void appendRoles(String role) {
        //System.out.println("Attempting to append role: " + role); // Log for debugging
        try {
            Platform.runLater(() -> {
                switch (role) {
                    case "[Mafia]":
                        roleBox.appendText("You are a " + role + "\nAs the Mafia, you have to kill the villagers in the night.\n");
                        break;
                    case "[Citizen]":
                        roleBox.appendText("You are a " + role + "\nAs the Villager, you have to find the Mafia and vote them out.\n");
                        break;
                    case "[Bodyguard]":
                        roleBox.appendText("You are a " + role + "\nAs the BodyGuard, you have to protect the villagers from the Mafia.\n");
                        break;
                    case "[Snitch]":
                        roleBox.appendText("You are a " + role + "\nAs the Snitch, you can see other players' roles.\n");
                        break;
                    default:
                        roleBox.appendText("Role " + role + " is not recognized.\n");
                        break;
                }
            });
        } catch (Exception e) {
            System.out.println("Exception in appendRoles: " + e.getMessage());
            e.printStackTrace(); // Print the stack trace to help with debugging
        }
    }

    public void clickOnAnchorPanes(MouseEvent mouseEvent) throws Exception {
        //get the name associated with the anchorpane
        String name = ((AnchorPane) mouseEvent.getSource()).getId();
        System.out.println(name);
        returnToMenu();

    }



}
