package dk.dtu.controller;

import dk.dtu.config;
import dk.dtu.model.AppModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;

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
    public TextArea infoTextField;
    private Map<String, Integer> userToIndexMap = new HashMap<>();
    @FXML
    public Label labelForSnitch11, labelForSnitch10, labelForSnitch9, labelForSnitch8, labelForSnitch7, labelForSnitch6, labelForSnitch5, labelForSnitch4, labelForSnitch3, labelForSnitch2, labelForSnitch1, labelForSnitch0;

    @FXML
    private AnchorPane User11, User10, User9, User8, User7, User6, User5, User4, User3, User2, User1, User0;
    @FXML
    Rectangle rectangleForSnitch0, rectangleForSnitch1, rectangleForSnitch2, rectangleForSnitch3, rectangleForSnitch4, rectangleForSnitch5, rectangleForSnitch6, rectangleForSnitch7, rectangleForSnitch8, rectangleForSnitch9, rectangleForSnitch10, rectangleForSnitch11;

    @FXML
    private Label labelForUser11, labelForUser10, labelForUser9, labelForUser8, labelForUser7, labelForUser6, labelForUser5, labelForUser4, labelForUser3, labelForUser2, labelForUser1, labelForUser0;

    public AppController() throws IOException {
        model = new AppModel();
    }

    @FXML
    private void initialize() {

        model.listenforRoleUpdate(this, config.getUsername());
        model.startListeningForMessages(messageArea);
        model.startListeningForDayNightCycle(this, config.getUsername());
        model.startListeningForTimeUpdate(this, config.getUsername());
        model.startListeningForUserUpdates(usernameList, config.getUsername());
        model.startListenForSnitchUpdate(this,config.getUsername());
        model.startListenForKilled(config.getUsername());
        Platform.runLater(() -> putUsersInCircles(config.getUserList()));
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

    public void updateDayNightCycle(String state, String killed) {
        System.out.println("Received state");
        Platform.runLater(() -> {
            if ("Day".equals(state)) {
                messageArea.setVisible(true);
                counter.setImage(sun);
                background.setImage(day);
                showKilled(killed);
                removeKilled(killed);
            } else if ("VotingTime".equals(state)) {
                infoTextField.setText("Voting time");
                counter.setImage(moon);
                background.setImage(day);
            } else if ("Night".equals(state)) {
                infoTextField.setText("");
                showKilled(killed);
                removeKilled(killed);
                config.setHasVoted(false);
                if(!Objects.equals(config.getRole(), "[Mafia]")){
                    messageArea.setVisible(false);
                }
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
                    case "[Mafia]" ->
                            roleBox.appendText("You are a " + role + "\nAs the Mafia, you have to kill the villagers in the night.\n");
                    case "[Citizen]" ->
                            roleBox.appendText("You are a " + role + "\nAs the Villager, you have to find the Mafia and vote them out.\n");
                    case "[Bodyguard]" ->
                            roleBox.appendText("You are a " + role + "\nAs the BodyGuard, you have to protect the villagers from the Mafia.\n");
                    case "[Snitch]" ->
                            roleBox.appendText("You are a " + role + "\nAs the Snitch, you can see other players' roles.\n");
                    default -> roleBox.appendText("Role " + role + " is not recognized.\n");
                }
            });
        } catch (Exception e) {
            System.out.println("Exception in appendRoles: " + e.getMessage());
        }
    }

    public void putUsersInCircles(String userList){
        //split the userList into an array
        String[] users = userList.split(", ");
        System.out.println("User list from appController" + Arrays.toString(users));

        AnchorPane[] anchorPanes = {User11, User10, User9, User8, User7, User6, User5, User4, User0, User3, User2, User1};
        Label[] labels = {labelForUser11, labelForUser10, labelForUser9, labelForUser8, labelForUser7, labelForUser6, labelForUser5, labelForUser4, labelForUser0, labelForUser3, labelForUser2, labelForUser1};

        //put the users in the circles
        for (int i = 0; i < users.length; i++) {
            anchorPanes[i].setVisible(true);
            anchorPanes[i].setDisable(false);
            labels[i].setText(users[i]);
            userToIndexMap .put(users[i],i);
            // Debugging output
            System.out.println("Assigning " + users[i] + " to circle " + anchorPanes[i].getId() + " and label " + labels[i].getId());
        }
    }

    private void removeKilled(String killed) {
        AnchorPane[] anchorPanes = {User11, User10, User9, User8, User7, User6, User5, User4, User0, User3, User2, User1};
        Label[] labels = {labelForUser11, labelForUser10, labelForUser9, labelForUser8, labelForUser7, labelForUser6, labelForUser5, labelForUser4, labelForUser0, labelForUser3, labelForUser2, labelForUser1};

        //put the users in the circles
        for (int i = 0; i < labels.length; i++) {
            if (labels[i].getText().equals(killed)) {
                anchorPanes[i].setVisible(false);
                anchorPanes[i].setDisable(true);
                labels[i].setText("");
            }
        }
    }
    public void AttemptAction(MouseEvent mouseEvent) throws InterruptedException {
        // Get the id of the clicked circle
        String circleId = ((AnchorPane) mouseEvent.getSource()).getId();
        String labelId = "labelFor" + circleId;
        // Find the label in the scene graph
        Label label = (Label) ((Node) mouseEvent.getSource()).getScene().lookup("#" + labelId);
        if (label != null) {
            // Print the text of the label
            System.out.println(label.getText());
        } else {
            System.out.println("Label not found for " + circleId);
        }
        assert label != null;
        model.AttemptAction(config.getUsername(),config.getRole(),label.getText());
    }

    public void showKilled(String killed) {
        Platform.runLater(() -> infoTextField.setText(killed + " was killed"));
    }

    public void updateSnitchMessage(String snitcher, String usernameOfSnitched, String roleOfVictim) {
        System.out.println("I was here" + snitcher + usernameOfSnitched + roleOfVictim);
        Label[] snitchLabels = {labelForSnitch11, labelForSnitch10, labelForSnitch9, labelForSnitch8, labelForSnitch7, labelForSnitch6, labelForSnitch5, labelForSnitch4, labelForSnitch3, labelForSnitch2, labelForSnitch1, labelForSnitch0};
        Rectangle[] snitchRectangles = {rectangleForSnitch11, rectangleForSnitch10, rectangleForSnitch9, rectangleForSnitch8, rectangleForSnitch7, rectangleForSnitch6, rectangleForSnitch5, rectangleForSnitch4, rectangleForSnitch3, rectangleForSnitch2, rectangleForSnitch1, rectangleForSnitch0};
        if (snitcher.equals("[Snitch]")) {
            Integer index = userToIndexMap.get(usernameOfSnitched);

            if (index != null && index >= 0 && index < snitchLabels.length) {
                // Update the label and rectangle for the snitched user
                snitchLabels[index].setText(roleOfVictim);
                snitchLabels[index].setVisible(true);
                snitchRectangles[index].setVisible(true);
                System.out.println("Snitch message received: " + usernameOfSnitched + " is a " + roleOfVictim);
            }
        }
    }
}


