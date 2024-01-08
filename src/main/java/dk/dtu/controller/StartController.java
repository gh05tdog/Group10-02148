
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
import javafx.stage.Stage;
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    public Button JoinLobby;
    public Button CreateLobby;
    public Button JoinGameIP;
    public TextField IpField;

    public StartController() throws IOException {
        AppModel model = new AppModel();
    }


    @FXML
    public void JoinLobbyAction(ActionEvent event) {
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
    private void CreateLobbyAction(ActionEvent event) {
        try {
            Server server = new Server();
            server.startServer();
            loadChatUI(event);
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }

    public void JoinGameBasedOnIP(ActionEvent actionEvent) {
        try {
            config.setIp(IpField.getText());
            //Try to connect to the server
            AppModel model = new AppModel();
            try {
                model.setServer(config.getIp());
                model.getServer().get(new ActualField("server"), new FormalField(String.class));
            } catch (Exception e) {
                System.out.println("Error: " + e);
                PopUpController.showPopUp("Could not connect to the server");
                return;
            }

            model.killServer();
            loadChatUI(actionEvent);
        } catch (Exception e) {
            System.out.println("error: " + e);
        }
    }

    private void loadChatUI(ActionEvent event) throws Exception {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/App_view.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setOnCloseRequest(e -> Platform.exit());
        currentStage.setScene(new Scene(newRoot));
    }

    public void openStartScreen() throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));
        Stage stage = new Stage();
        stage.setTitle("MoonLit Noir");
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }
}
