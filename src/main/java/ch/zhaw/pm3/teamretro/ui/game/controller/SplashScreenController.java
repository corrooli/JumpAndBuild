package ch.zhaw.pm3.teamretro.ui.game.controller;

import java.io.IOException;

import ch.zhaw.pm3.teamretro.ui.common.MessageHandler;
import ch.zhaw.pm3.teamretro.ui.editor.view.EditorView;
import ch.zhaw.pm3.teamretro.ui.game.view.GameView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Controller class of the splash screen. Gives the player an entry point to
 * choose whether to simply play a game or launch the editor.
 * <p>
 * This controller is deliberately kept simple, and it does not handle any
 * business logic. This ensures a correct MVC/MVP pattern. All information on
 * the UI is updated via Listeners.
 */
public class SplashScreenController {
    /**
     * Root node of the splash screen.
     */
    @FXML
    public BorderPane root;

    /**
     * Button for launching the editor.
     */
    @FXML
    public Button launchEditorButton;

    /**
     * Button for launching the game portion.
     */
    @FXML
    public Button playButton;

    /**
     * Button for exiting the entire application.
     */
    @FXML
    public Button exitButton;

    /**
     * Launches the game application.
     */
    private void startGame() {
        GameView gameView = new GameView();
        root.getScene().getWindow().hide();
        try {
            gameView.start(new Stage());
        } catch (IOException e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Starts the game portion of the application.
     */
    @FXML
    public void startGameAction() {
        startGame();
    }

    /**
     * Launches the editor application.
     */
    @FXML
    public void launchEditorAction() {
        EditorView editorView = new EditorView();
        root.getScene().getWindow().hide();
        try {
            editorView.start(new Stage());
        } catch (IOException e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Closes the entire application.
     */
    @FXML
    public void exitAction() {
        ((Stage) root.getScene().getWindow()).close();
    }
}
