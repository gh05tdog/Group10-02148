package dk.dtu;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    public static void main(String[] argv) {
        launch(argv);
    }

    @Override
    public void start(Stage stage) throws Exception {
        openStartScreen(stage);
    }

    public void openStartScreen(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));

        // Set icon
        Image icon = new Image("/dk/dtu/view/images/moonlit_icon.png");

        stage.getIcons().add(icon);

        Scene scene = new Scene(root);
        stage.setTitle("MoonLit Noir");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }
}
