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
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class PopUpIPController {
    public TextField IpField;
    public TextField UserNameField;
    public Rectangle joinGameRectangle;

    @FXML
    public void JoinGameBasedOnIP(MouseEvent actionEvent) throws IOException {
        if (UserNameField.getText().isEmpty()) {
            System.out.println("Username is required to join the lobby.");
        }else {
            //Set ip

            //TODO: Check if username is already taken
            //TODO: If not, set username


            String ip = IpField.getText();
            config.setIp(ip);
            config.setUsername(UserNameField.getText());
            System.out.println(UserNameField.getText());
            System.out.println("Username: " + config.getUsername());
            loadLobbyUI(actionEvent);
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
