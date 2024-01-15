package dk.dtu.controller;

import dk.dtu.config;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.FormalField;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.Objects;

public class PopUpIPController {
    public TextField IpField;
    public TextField UserNameField;

    @FXML
    public void JoinGameBasedOnIP(MouseEvent actionEvent) throws IOException {
        if (UserNameField.getText().isEmpty()) {
            System.out.println("Username is required to join the lobby.");
        } else {
            //TODO: Check if username is already taken
            //TODO: If not, set username


            String ip = IpField.getText();
            if (connectToServer(ip)) {
                if (checkUsernameIsValid(UserNameField.getText())) {
                    config.setIp(ip);
                    config.setUsername(UserNameField.getText());
                    System.out.println(UserNameField.getText());
                    System.out.println("Username: " + config.getUsername());
                    loadLobbyUI(actionEvent);
                } else {
                    System.out.println("Username is already taken");
                    UserNameField.setText("Username is already taken");
                }
            } else {
                System.out.println("Could not connect to server");
                IpField.setText("Could not connect to server");
            }
        }
    }

    private boolean checkUsernameIsValid(String text) {
        //TODO: Fix username check
        return true;

    }

    private boolean connectToServer(String ip) {
        config.setIp(ip);
        try {
            //Check for valid ip
            new RemoteSpace(config.getIp() + "/chat?keep");
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public void loadLobbyUI(MouseEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/lobby.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
    }

    public void returnToMenu(MouseEvent mouseEvent) throws IOException {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));
        Stage currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
    }
}
