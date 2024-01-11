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
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    public AnchorPane joinGamePane;
    public AnchorPane createGamePane;
    public AnchorPane join;

    @FXML
    public AnchorPane returnPane;
    public TextField IpField;
    public Rectangle joinGameRectangle;
    public TextField UserNameField;


    @FXML
    public void JoinLobbyAction(MouseEvent event) {
        try {

            // Load the new FXML file
            Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/PopUpIP.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setOnCloseRequest(e -> Platform.exit());
            currentStage.setScene(new Scene(newRoot));
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    @FXML
    private void returnToMenu() throws Exception {
        Stage stage = (Stage) returnPane.getScene().getWindow();
        stage.setScene(App.getScene());

    }

    @FXML
    private void CreateLobbyAction(MouseEvent event) {
        try {
            Server server = new Server();
            server.startServer();
            loadLobbyUI(event);
        } catch (Exception e) {
            System.out.println("Error creating lobby: " + e);
        }
    }

    public void loadLobbyUI(MouseEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/lobby.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));

    }


}
