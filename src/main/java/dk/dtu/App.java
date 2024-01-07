package dk.dtu;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
<<<<<<< Updated upstream
        Parent root = FXMLLoader.load(getClass().getResource("/dk/dtu/view/background.fxml"));


=======
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/dk/dtu/view/StartScreen.fxml")));

        Image icon = new Image("/dk/dtu/view/images/moonlit_icon.png");
        stage.getIcons().add(icon);
>>>>>>> Stashed changes
        Scene scene = new Scene(root);

        stage.setTitle("JSpace JavaFX Example");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] argv) {
        launch(argv);
    }
}
