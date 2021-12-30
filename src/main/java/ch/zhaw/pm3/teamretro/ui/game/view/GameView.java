package ch.zhaw.pm3.teamretro.ui.game.view;

import java.io.FileInputStream;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * View class for the Editor. Prepares the FXMLLoader and initializes the main
 * GUI.
 */
public class GameView extends Application {

    /**
     * Parent root node.
     */
    Parent root;

    /**
     * Starts the game by creating a scene.
     *
     * @param stage Initial stage.
     * @throws IOException In case the fxml file couldn't be found.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        String mainWindowPath = "src/main/resources/game.fxml";
        FileInputStream mainWindowStream = new FileInputStream(mainWindowPath);

        root = (VBox) loader.load(mainWindowStream);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);

        stage.show();
    }
}
