package dk.dtu.controller;

import dk.dtu.Server;
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
    public void JoinGameBasedOnIP(MouseEvent actionEvent) throws IOException, InterruptedException {
        if (UserNameField.getText().isEmpty()) {
            System.out.println("Username is required to join the lobby.");
        } else if (UserNameField.getText().length() > 20) {
            System.out.println("Username is too long.");
        } else {
            RemoteSpace server = new RemoteSpace("tcp://" + IpField.getText() + "/game?keep");
            System.out.println("Connected to server");
            server.get(new ActualField("lock"));
            System.out.println("Got lock");
            server.put("usernameCheck", UserNameField.getText());
            System.out.println("Sent username check");
            Object[] response = server.get(new ActualField("usernameCheck"), new FormalField(Boolean.class));
            if ((response[1]).equals(false)) {
                System.out.println("Username is taken");
                server.put("lock");
                server.close();
                UserNameField.setText("Username is taken");
                return;
            }
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
        currentStage.setOnCloseRequest(e -> closeProgram() );
        currentStage.setScene(new Scene(newRoot));
    }

    private void closeProgram() {
        //Send message to server that the user has left the lobby
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

    public void loadMainMenu(AppController appController) throws IOException {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));
        Stage currentStage = (Stage) appController.messageArea.getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
        //close the connection to the server
        config.setIp("");
        config.setUsername("");
        config.setRole("");
        config.setLobbyLeader(false);
        config.setHasVoted(false);
        config.setUserList("");
        Server.stopServer();
    }
}
