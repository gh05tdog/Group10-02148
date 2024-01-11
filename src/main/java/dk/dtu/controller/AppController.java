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

public class AppController {
    public ImageView background;

    public TextField messageField;
    public TextArea messageArea;
    public TextArea usernameList;
    public ImageView counter;
    public Label timerLabel;

    public AnchorPane settings;

    private final AppModel model;

    public Image moon = new Image("/dk/dtu/view/images/Moon.png");

    public Image sun = new Image("/dk/dtu/view/images/sun.png");

    public Image day = new Image("/dk/dtu/view/images/moonlit_main_day.jpg");

    public Image night = new Image("/dk/dtu/view/images/moonlit_main_night.jpg");

    public AppController() throws IOException, InterruptedException {
        model = new AppModel();
        RandomSpace testSpace = new RandomSpace(name,);

        testSpace.put("Johhny");

    }

    @FXML
    private void initialize() {
//        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateDayNightCycle("day")));
//        timeline.setCycleCount(Timeline.INDEFINITE);
//        timeline.play();
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

    @FXML
    private void returnToMenu() throws Exception {
        Stage stage = (Stage) settings.getScene().getWindow();
        stage.setScene(App.getScene());

    }
}
