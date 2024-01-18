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

    //This method makes sure, that only 1 person can try to join at a time with an IP, and makes sure that no one can have the same username
    @FXML
    public void JoinGameBasedOnIP(MouseEvent actionEvent) throws IOException, InterruptedException {
        if (UserNameField.getText().isEmpty()) {
            UserNameField.setText("Please enter a username");
        } else if (UserNameField.getText().length() > 20) {
            UserNameField.setText("Username is too long");
        } else {
            //This connects to the server, so that the user can try to get the lock
            RemoteSpace server = new RemoteSpace("tcp://" + IpField.getText() + "/game?keep");

            server.get(new ActualField("lock"));

            //If the user got the lock, it then checks if the username is taken
            server.put("usernameCheck", UserNameField.getText());

            //If the username is taken, it will tell the user and put the lock back in the server for the next player.
            Object[] response = server.get(new ActualField("usernameCheck"), new FormalField(Boolean.class));
            if ((response[1]).equals(false)) {
                server.put("lock");
                server.close();
                UserNameField.setText("Username is taken");
                return;
            }

            String ip = IpField.getText();
            config.setIp(ip);
            config.setUsername(UserNameField.getText());

            loadLobbyUI(actionEvent);
        }
    }


    public void loadLobbyUI(MouseEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/lobby.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> closeProgram());
        currentStage.setScene(new Scene(newRoot));
    }


    private void closeProgram() {
        try {
            RemoteSpace server = new RemoteSpace("tcp://" + IpField.getText() + "/game?keep");
            server.put("leaveLobby", config.getUsername());
        } catch (Exception e) {
            System.out.println("Error creating lobby: " + e);
        }
        Platform.exit();
        System.exit(0);
    }

    public void returnToMenu(MouseEvent mouseEvent) throws IOException {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));
        Stage currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
    }

}
