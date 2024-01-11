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


    public Image moon = new Image("/dk/dtu/view/images/Moon.png");

    public Image sun = new Image("/dk/dtu/view/images/sun.png");

    public Image day = new Image("/dk/dtu/view/images/moonlit_main_day.jpg");

    public Image night = new Image("/dk/dtu/view/images/moonlit_main_night.jpg");

    public AppController() throws IOException {
        model = new AppModel();
        this.testSpace = new RandomSpace();
    }

    @FXML
    private void initialize() throws InterruptedException {
        model.startListeningForMessages(messageArea);
        model.startListeningForDayNightCycle(this, config.getUsername());
        model.startListeningForUserUpdates(usernameList, config.getUsername());
        System.out.println(config.getUsername());
        put();

    }

    public void handleSendAction() throws InterruptedException {
        getAndPrint();
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

    public void put() throws InterruptedException {
        testSpace.put("Username 1");
        testSpace.put("Username 2");
        testSpace.put("Username 3");
        testSpace.put("Username 4");
        testSpace.put("Username 5");
        testSpace.put("Username 6");
        testSpace.put("Username 7");
        testSpace.put("Username 8");
        testSpace.put("Username 9");
        testSpace.put("Username 10");
    }

    public void getAndPrint() throws InterruptedException {
        while (testSpace.size() > 0) {
            Object[] t = testSpace.get(new FormalField(String.class));
            System.out.println(t[0]);
        }
    }
}
