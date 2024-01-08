package dk.dtu.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class PopUpController {

    @FXML
    private Label messageLabel; // Ensure that there's a Label in PopUpView.fxml with fx:id="messageLabel"

    // Method to update the text of the popup
    public void setText(String text) {
        messageLabel.setText(text);
    }

    // Method to display the popup
    public static void showPopUp(String text) {
        Platform.runLater(() -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(PopUpController.class.getResource("/dk/dtu/view/PopUpView.fxml"));
                AnchorPane popupPane = fxmlLoader.load();

                // Get the controller and set the text
                PopUpController controller = fxmlLoader.getController();
                controller.setText(text);

                Scene scene = new Scene(popupPane);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL); // Set the stage as a modal window
                stage.setScene(scene);
                stage.showAndWait(); // Show the popup and wait for it to be closed
            } catch (IOException e) {
                System.out.println("Error: " + e);
            }
        });
    }
}
