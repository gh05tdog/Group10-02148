package dk.dtu.controller;

import dk.dtu.Server;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    public Button JoinLobby;
    public Button CreateLobby;
    public Button JoinGameIP;
    public TextField IpField;
    private AppModel model;
    private Server server;

    public StartController() throws IOException {

    }

    @FXML
    public void JoinLobbyAction(ActionEvent event) {
        try{
            Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/PopUpIP.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setOnCloseRequest(e -> Platform.exit());
            currentStage.setScene(new Scene(newRoot));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @FXML
    private void CreateLobbyAction(ActionEvent event) {
        try {
            server = new Server(); // Instantiate the Server here
            server.startServer();  // Start the server
            loadChatUI(event);
        } catch (Exception e) {
            System.out.println("Error creating lobby: " + e);
        }
    }

    private void loadChatUI(ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/App_view.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
    }

    public void openStartScreen() throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));
        Stage stage = new Stage();
        stage.setTitle("MoonLit Noir");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }


    public void JoinGameBasedOnIP(ActionEvent actionEvent) {
        try {
            //Set ip
            String ip = IpField.getText();
            config.setIp(ip);
            loadChatUI(actionEvent);
        } catch (Exception e) {
            System.out.println("Error joining lobby: " + e);
        }
    }
}
