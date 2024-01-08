
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

    public StartController() throws IOException {
        AppModel model = new AppModel();
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
            Server server = new Server();
            server.startServer();
            loadChatUI(event);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public void JoinGameBasedOnIP(MouseEvent mouseEvent) {
        try {
            config.setIp(IpField.getText());
            loadChatUI(mouseEvent);
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
    }

    private void
    loadChatUI(MouseEvent event) throws IOException {

        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/App_view.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
    }
}
