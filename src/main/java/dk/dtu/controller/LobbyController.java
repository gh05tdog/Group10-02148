package dk.dtu.controller;

import dk.dtu.config;
import dk.dtu.model.AppModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LobbyController {
    private final AppModel model;
    public TextArea usernameList;
    public AnchorPane StartGameButton;
    public TextArea messageAreaLobby;
    public TextField usernameField;
    public AnchorPane connectButton;
    @FXML
    private TextField messageField;


    public LobbyController() throws IOException {
        model = new AppModel();
        //Makes an instance of AppModel
    }

    //Initialize the lobby with the buttons set to invisible
    @FXML
    private void initialize() throws InterruptedException {
        if (config.getUsername() != null) {
            //Remove the connect button for the user that joins via the IP
            connectButton.setVisible(false);
            usernameField.setVisible(false);
            //Join the lobby in the backend
            handleConnectAction();

            //Checks whether the game has started
            Platform.runLater(() -> {
                Stage currentStage = (Stage) messageAreaLobby.getScene().getWindow();
                if (currentStage == null) {
                    System.out.println("Stage is null");
                } else {
                    model.startListeningForGameStart(currentStage);
                }
                // If the user is the lobby leader, make the start game button visible
                StartGameButton.setVisible(config.getLobbyLeader());

            });
        }
    }

    // When you click the send button, send the message to every other player.
    @FXML
    private void handleSendAction() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                model.sendMessage(config.getUsername(), message, "lobby");
            } catch (Exception e) {
                System.out.println("Error sending message: " + e);
            }
            messageField.clear();
        }
    }

    //Handle connection from the player that creates the game.
    public void handleConnectAction() throws InterruptedException {
        if (usernameField.isVisible()) {
            config.setUsername(usernameField.getText());
        }

        usernameField.setVisible(false);
        connectButton.setVisible(false);

        //Join the lobby in the backend
        model.joinLobby(config.getUsername());
        //Starts the listening thread for messages, so they can be displayed
        model.startListeningForMessages(messageAreaLobby);
        // Start listening for user updates - meaning displaying the list of users to the right
        model.startListeningForUserUpdates(usernameList, config.getUsername());

    }

    //This runs, when the startGame button is pressed, and it then switches the view to App_view.fxml
    @FXML
    private void StartGameAction(MouseEvent event) throws IOException, InterruptedException {
        // Check if the lobby leader has a username
        if(config.getUsername() == null){
            messageAreaLobby.appendText("You need to pick a username, and click connect! \n");
            return;
        }
        model.startGame();
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/App_view.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
    }
}
