package ch.zhaw.pm3.teamretro.ui.editor.view;

import java.io.FileInputStream;
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * View class for the Editor. Prepares the FXMLLoader and initializes the main
 * GUI.
 */
public class EditorView extends Application {

    /**
     * Parent root node.
     */
    Parent root;

    /**
     * Starts the application by creating a scene.
     *
     * @param stage Initial stage.
     * @throws IOException In case the fxml file couldn't be found.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        String mainWindowPath = "src/main/resources/editor.fxml";
        FileInputStream mainWindowStream = new FileInputStream(mainWindowPath);

        root = (VBox) loader.load(mainWindowStream);

        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.getIcons().add(new Image("icon/icon.png"));
        stage.setTitle("Jump and Build Editor");
        stage.setScene(scene);
        stage.show();
    }

}
