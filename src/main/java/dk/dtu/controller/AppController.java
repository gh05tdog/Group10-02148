package dk.dtu.controller;

import dk.dtu.config;
import dk.dtu.model.AppModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class AppController {
    public ImageView background;

    public TextField messageField;
    public TextArea messageArea;
    public TextArea usernameList;
    public ImageView counter;
    public Label timerLabel;

    private final AppModel model;

    public Image moon = new Image("/dk/dtu/view/images/Moon.png");

    public Image sun = new Image("/dk/dtu/view/images/sun.png");

    public Image day = new Image("/dk/dtu/view/images/moonlit_main_day.jpg");

    public Image night = new Image("/dk/dtu/view/images/moonlit_main_night.jpg");
    public TextArea roleBox;

    public AppController() throws IOException {
        model = new AppModel();
    }

    @FXML
    private void initialize() {

        model.startListeningForMessages(messageArea);
        model.startListeningForDayNightCycle(this, config.getUsername());
        model.startListeningForUserUpdates(usernameList, config.getUsername());
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

}
