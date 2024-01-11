package dk.dtu.controller;

import dk.dtu.App;
import dk.dtu.PlayerHandler;
import dk.dtu.config;
import dk.dtu.model.AppModel;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.jspace.FormalField;
import org.jspace.RandomSpace;
import org.jspace.Space;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class LobbyController {
    public TextArea usernameList;
    public TextField chatroomField;
    public Button StartGameButton;
    public TextArea messageAreaLobby;
    public TextField usernameField;
    public Button connectButton;
    public Button returnButton;

    @FXML
    private TextField messageField;

    private final AppModel model;

    public Space testSpace;


    public LobbyController() throws IOException {
        model = new AppModel();
        this.testSpace = new RandomSpace();
    }

    @FXML
    private void initialize() throws InterruptedException {
        if (config.getUsername() != null) {
            //Remove the connect button
            connectButton.setVisible(false);
            usernameField.setVisible(false);
            chatroomField.setVisible(false);
            //Join the lobby
            handleConnectAction();
            System.out.println("Username: " + config.getUsername());
        }

    }


    // When you click the send button, send the message
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

    //Handle the connect button
    public void handleConnectAction() throws InterruptedException {
        if (usernameField.isVisible()) {
            config.setUsername(usernameField.getText());
        }

        usernameField.setVisible(false);
        chatroomField.setVisible(false);
        connectButton.setVisible(false);


        //System.out.println("Username: " + config.getUsername());

        //Join the lobby
        model.joinLobby(config.getUsername());
        // Start listening for messages
        model.startListeningForMessages(messageAreaLobby);
        // Start listening for user updates
        model.startListeningForUserUpdates(this,usernameList, config.getUsername());
    }

    //Handle the start game button
    @FXML
    private void StartGameAction(ActionEvent event) throws IOException, InterruptedException {
        model.startGame();
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/App_view.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
        setOnMap(newRoot);

    }

    @FXML
    private void returnToMenu(ActionEvent event) throws Exception {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));

    }

    public void setOnMap(Parent scene) throws InterruptedException, IOException {
        int i = 0;
        while (testSpace.size() > 0) {
            i++;
            Object[] t = testSpace.get(new FormalField(String.class));
            System.out.println("who am i: " + Arrays.toString(t));
            String anchorPaneId = "#Anchor" + i; // Construct the ID
            AnchorPane anchorPane = (AnchorPane) scene.getScene().lookup(anchorPaneId); // Get the AnchorPane

            if(anchorPane != null) {
                System.out.println("AnchorPane found");
                anchorPane.setVisible(true);
                anchorPane.setDisable(false);
            } else {
                System.out.println("AnchorPane not found");
            }
        }
    }

    public void getAndPrint() throws InterruptedException {
        while (testSpace.size() > 0) {
            Object[] t = testSpace.get(new FormalField(String.class));
            System.out.println(t[0]);
        }
    }

}
