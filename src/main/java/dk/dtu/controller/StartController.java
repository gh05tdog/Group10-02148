
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
import org.jspace.ActualField;
import org.jspace.FormalField;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    public AnchorPane joinGamePane;
    public AnchorPane createGamePane;
    public AnchorPane join;

    @FXML
    public AnchorPane returnPane;
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
    private void returnToMenu() throws Exception {
        Stage stage = (Stage) returnPane.getScene().getWindow();
        stage.setScene(App.getScene());

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
