package ch.zhaw.pm3.teamretro.ui.game.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ch.zhaw.pm3.teamretro.gamepack.GamePackFactory;
import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.logic.common.CanvasController;
import ch.zhaw.pm3.teamretro.logic.common.KeyEventHandler;
import ch.zhaw.pm3.teamretro.logic.game.GameLoop;
import ch.zhaw.pm3.teamretro.ui.common.FileBrowser;
import ch.zhaw.pm3.teamretro.ui.common.InteractionWindow;
import ch.zhaw.pm3.teamretro.ui.common.MessageHandler;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Game UI Controller. Contains all elements to control the game.
 * <p>
 * This controller is deliberately kept simple, and it does not handle any
 * business logic. This ensures a correct MVC/MVP pattern. All information on
 * the UI is updated via Listeners.
 */
public class GameController {
    /**
     * Debug Straight-To-Game boolean. If true, game loop launches directly without
     * showing the splash screen.
     */
    private static final boolean DEBUG_STRAIGHT_TO_GAME = false;

    /**
     * GameLoop instance. Does all the magic and controls the game flow.
     */
    private GameLoop gameLoop;

    /**
     * Root node of the UI.
     */
    @FXML
    public VBox root;

    /**
     * Main canvas instance. Used to draw images on the screen.
     */
    @FXML
    public Canvas canvas;

    /**
     * Displays lives, score and current level.
     */
    @FXML
    public Label scoreBar;

    /**
     * Number of lives the player has. Only for UI purposes.
     */
    private int lives = 0;

    /**
     * Score the player. Only for UI purposes.
     */
    private int score = 0;

    /**
     * Number of all levels in GamePack. Only for UI purposes.
     */
    private int numberOfLevelsInGamePack;

    /**
     * Number of current level (which is being played). Only for UI purposes.
     */
    private int numberOfCurrentLevel;

    /**
     * Currently chosen level. Only for UI purposes.
     */
    private String chosenLevel;

    /**
     * List of levels in the GamePack.
     */
    List<String> levelList = new ArrayList<>();

    /**
     * Handles all keyboard-related events.
     */
    KeyEventHandler keyEventHandler;

    /**
     * Controls the canvas and provides easy interfacing with graphics.
     */
    CanvasController canvasController;

    /**
     * Default JavaFX initialization method. Will be called automatically after
     * startup.
     */
    @FXML
    private void initialize() {
        onInitializeCanvas();
    }

    /**
     * Instantiation of the CanvasController.
     */
    private void onInitializeCanvas() {
        // Necessary, since right after launching the UI root is still null
        Platform.runLater(() -> {
            waitUntilRootIsInitialized();
            initializeControllersAndModel();
            openGamePackAction();
            if (DEBUG_STRAIGHT_TO_GAME) {
                startDebugMode();
            }
        });
    }

    /**
     * Busy waiting loop. Needs to be done this way to make sure the Scene instance
     * isn't null when creating a new CanvasController.
     */
    private void waitUntilRootIsInitialized() {
        while (root == null) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                MessageHandler.handleException(e);
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Initializes other controllers and model.
     */
    private void initializeControllersAndModel() {
        keyEventHandler = new KeyEventHandler();
        keyEventHandler.setupActionHandler(root.getScene());
        canvasController = new CanvasController(canvas);
        gameLoop = new GameLoop(canvasController, keyEventHandler);
    }

    /**
     * Opening a GamePack and starting the game.
     */
    @FXML
    private void openGamePackAction() {
        try {
            if (chooseGamePack()) {
                lives = gameLoop.getLives().get();
                refreshStatusBar();
                runGameLoop();
            }
        } catch (IOException | InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Restarting a level.
     */
    @FXML
    private void restartLevelAction() {
        try {
            gameLoop.restartLevel();
        } catch (IOException | InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Quitting the game.
     */
    @FXML
    private void quitGameAction() {
        if (InteractionWindow.ask("Quit game", "You really want to quit?")) {
            gameLoop.stop();
            ((Stage) root.getScene().getWindow()).close();
        }
    }

    /**
     * Starts the game.
     *
     * @throws IOException               In case the requested files couldn't be
     *                                   found.
     * @throws InvalidLevelConfiguration In case the level has illegal
     *                                   configuration.
     */
    private void runGameLoop() throws IOException, InvalidLevelConfiguration {
        registerGameLoopListeners();
        gameLoop.run(false);
    }

    /**
     * Register Listeners to important GameLoop objects. Defines callbacks when
     * score and lives were changed, and when the level was won.
     */
    private void registerGameLoopListeners() {
        gameLoop.getScore().addListener(this::scoreHandler);
        gameLoop.getLives().addListener(this::livesHandler);
        gameLoop.getLevelWasWonProperty().addListener(this::winHandler);
    }

    /**
     * Stops the Game Loop.
     */
    private void stopGame() {
        score = 0;
        gameLoop.stop();
        gameLoop.getLevelWasWonProperty().set(false);
    }

    /**
     * Chooses a GamePack by using a file browser.
     *
     * @return True if successful, false if it failed
     * @throws IOException In case the requested files couldn't be found.
     */
    private boolean chooseGamePack() throws IOException {
        FileBrowser fileBrowser = new FileBrowser(root.getScene());
        if (fileBrowser.openFile("zip") && chooseLevel(fileBrowser.getFileName())) {
            gameLoop.setGamePack(fileBrowser.getFileName());
            return true;
        }
        return false;
    }

    /**
     * Chooses a level out of a GamePack.
     *
     * @param path File path of GamePack on disk.
     * @return True if successful, false if it failed
     * @throws IOException In case there is an issue with the file.
     */
    private boolean chooseLevel(String path) throws IOException {
        levelList = GamePackFactory.getLevelNames(path);
        Optional<Integer> chosenLevelNumber = InteractionWindow.choose(levelList, "Choose level",
                "Choose the level you want to play:");
        if (chosenLevelNumber.isEmpty()) {
            return false;
        }
        chosenLevel = levelList.get(chosenLevelNumber.get());
        numberOfLevelsInGamePack = levelList.size();

        numberOfCurrentLevel = chosenLevelNumber.get();
        gameLoop.setLevel(chosenLevel);
        return true;
    }

    /**
     * Refreshes the status bar.
     */
    private void refreshStatusBar() {
        scoreBar.setText(String.format("%s (%d/%d) | Lives: %d | Score: %d", chosenLevel, numberOfCurrentLevel + 1,
                numberOfLevelsInGamePack, lives, score));
    }

    /**
     * For debugging purposes. Skips the file and level selection and starts the
     * built-in master GamePack.
     */
    private void startDebugMode() {
        gameLoop.setGamePack("src/main/resources/master.zip");
        gameLoop.setLevel("startlevel");
        try {
            runGameLoop();
        } catch (IOException | InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Handles the event of a won level or game.
     *
     * @param observable Observable object. Unused but needed for method
     *                   declaration.
     * @param oldValue   Previous value. Unused but needed for method declaration.
     * @param newValue   Changed value. Unused but needed for method declaration.
     */
    private void winHandler(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (Boolean.TRUE.equals(newValue)) { // validation against null required (recommended by SonarLint)
            Platform.runLater(() -> {
                // Level won!
                if (numberOfCurrentLevel == numberOfLevelsInGamePack - 1) {
                    InteractionWindow.inform("Congratulations!",
                            "You've beaten all levels in this GamePack!\nThank you for playing! You're the chaddest!");
                    stopGame();
                } else {
                    InteractionWindow.inform("Won level!", "Next up: " + levelList.get(numberOfCurrentLevel + 1));
                    prepareNextLevel();
                }
            });
        }
    }

    /**
     * Loading the next level and calling the GameLoop to execute it.
     */
    private void prepareNextLevel() {
        try {
            ++numberOfCurrentLevel;

            // Load + set next level in GameLoop and refresh status bar
            chosenLevel = levelList.get(numberOfCurrentLevel);
            gameLoop.setLevel(levelList.get(numberOfCurrentLevel));
            gameLoop.getLevelWasWonProperty().set(false);
            refreshStatusBar();

            // Clear user input
            keyEventHandler.getCurrentlyActiveKeys().clear();

            gameLoop.run(false);
        } catch (IOException | InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
            stopGame();
        }
    }

    /**
     * Handles the UI side of the lives counter, as well as handling the game over
     * screen.
     *
     * @param observable Observable object. Unused but needed for method
     *                   declaration.
     * @param oldValue   Previous value. Unused but needed for method declaration.
     * @param newValue   Changed value.
     */
    private void livesHandler(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        Platform.runLater(() -> {
            if (newValue.intValue() < 0) {
                stopGame();
                refreshStatusBar();
                if (InteractionWindow.ask("Game Over!", "Better luck next time!\n\nDo you want to play again?")) {
                    initializeControllersAndModel();
                    openGamePackAction();
                }
                return;
            }

            lives = newValue.intValue();
            stopGame();
            InteractionWindow.inform("You lost!", "Unfortunately you died. Try again.");
            refreshStatusBar();
            restartLevelAction();
        });
    }

    /**
     * Handles the UI side of the score counter.
     *
     * @param observable Observable object.
     * @param oldValue   Previous value.
     * @param newValue   Changed value.
     */
    private void scoreHandler(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        score = newValue.intValue();
        refreshStatusBar();
    }

    /**
     * Opens a window with control instructions and development credits.
     */
    public void aboutMenuAction() {
        InteractionWindow.inform("About Jump & Build",
                String.format("Move with  ← and → or A and D%n%nJump with SPACE, ↑, W or J%n%nRun with SHIFT or K%n%n"
                        + "© 2020 Adrian Hornung, Severin Fürbringer, Oliver Corrodi, Sydney Nguyen and Nicolas Meier"));
    }
}
