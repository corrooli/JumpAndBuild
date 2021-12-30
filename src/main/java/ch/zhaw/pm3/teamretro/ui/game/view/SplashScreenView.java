package ch.zhaw.pm3.teamretro.ui.game.view;

import java.io.FileInputStream;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * View class for the Editor. Prepares the FXMLLoader and initializes the main
 * GUI.
 */
public class SplashScreenView extends Application {

    /**
     * Parent root node.
     */
    Parent root;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        String mainWindowPath = "src/main/resources/splashScreen.fxml";
        FileInputStream mainWindowStream = new FileInputStream(mainWindowPath);

        root = (BorderPane) loader.load(mainWindowStream);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Starting the UI. Will be called externally.
     */
    public void startUI() {
        Application.launch();
    }
}
