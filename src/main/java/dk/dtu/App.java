package dk.dtu;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        openStartScreen(stage);
    }

    public void openStartScreen(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));
        Scene scene = new Scene(root);
        stage.setTitle("MoonLit Noir");
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> Platform.exit());
        stage.show();
    }


    public static void main(String[] argv) {
        launch(argv);
    }
}
