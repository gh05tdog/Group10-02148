
package dk.dtu.controller;

import dk.dtu.App;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    public AnchorPane joinGamePane;
    public AnchorPane createGamePane;
    public Button JoinGameIP;
    public TextField IpField;
    private AppModel model;
    private Server server;

    public StartController() throws IOException {

    }

    @FXML
    public void JoinLobbyAction(MouseEvent event) {
        try {

            // Load the new FXML file
            Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/PopUpID.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setOnCloseRequest(e -> Platform.exit());
            currentStage.setScene(new Scene(newRoot));
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    @FXML
    private void CreateLobbyAction(MouseEvent event) {
        try {
            server = new Server(); // Instantiate the Server here
            server.startServer();  // Start the server
            loadChatUI(event);
        } catch (Exception e) {
            System.out.println("Error creating lobby: " + e);
        }
    }

    private void loadChatUI(MouseEvent event) throws IOException {



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
