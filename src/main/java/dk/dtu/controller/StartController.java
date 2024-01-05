package dk.dtu.controller;

import dk.dtu.config;

import dk.dtu.model.AppModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspace.RemoteSpace;

import java.io.IOException;
import java.util.Objects;

public class StartController {

    private RemoteSpace server;
    public Button JoinLobby;
    public Button CreateLobby;
    public Button JoinGameIP;
    public TextField IpField;

    private final AppModel model;

    public StartController() throws IOException {
        model = new AppModel();
    }


    @FXML
    public void JoinLobbyAction(ActionEvent event) {
        try {
            // Load the new FXML file
            Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/PopUPID.fxml")));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(newRoot));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @FXML
    private void CreateLobbyAction(ActionEvent event) throws IOException {
        //Set the server IP to the config file
        System.out.println("CreateLobbyAction");
        model.ChangeSeverIP(config.getIp());
        try {
            loadChatUI(event);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public void JoinGameBasedOnIP(ActionEvent actionEvent) throws IOException {
        try {
            config.setIp(IpField.getText());
            model.ChangeSeverIP(config.getIp());
            loadChatUI(actionEvent);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void loadChatUI(ActionEvent event) throws IOException {
        Parent newRoot = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/App_view.fxml")));
        Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        currentStage.setScene(new Scene(newRoot));
    }
}
