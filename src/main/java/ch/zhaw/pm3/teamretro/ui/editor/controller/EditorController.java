package ch.zhaw.pm3.teamretro.ui.editor.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import ch.zhaw.pm3.teamretro.gamepack.GamePackFactory;
import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.Level;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Animation;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;
import ch.zhaw.pm3.teamretro.logic.common.CanvasController;
import ch.zhaw.pm3.teamretro.logic.common.KeyEventHandler;
import ch.zhaw.pm3.teamretro.logic.editor.EditorLogic;
import ch.zhaw.pm3.teamretro.ui.common.FileBrowser;
import ch.zhaw.pm3.teamretro.ui.common.InteractionWindow;
import ch.zhaw.pm3.teamretro.ui.common.MessageHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main Editor Window UI Controller. Contains all elements to control the
 * Editor.
 * <p>
 * Displays blocks on the corresponding tab panes, can scroll the level, change
 * the level theme and can save and load levels.
 * <p>
 * This controller is deliberately kept simple, and it does not handle any
 * business logic. This ensures a correct MVC/MVP pattern. All information on
 * the UI is updated via Listeners.
 */
public class EditorController {
    /**
     * Regular expressions for valid level names to ensure platform compatibility.
     */
    private static final String LEVEL_NAME_REGEX = "^[A-Za-z0-9]([A-Za-z0-9 ]{0,200}[A-Za-z0-9])?$";

    /**
     * Pattern instance containing LEVEL_NAME_REGEX.
     */
    private static final Pattern LEVEL_NAME_PATTERN = Pattern.compile(LEVEL_NAME_REGEX);

    /**
     * Number of protected entries in the Level menu. Protected entries to make it
     * impossible to delete menu entries that are vital for the proper functionality
     * (only level in GamePack)
     */
    private static final int PROTECTED_ENTRIES_LEVEL_MENU = 3;

    /**
     * Amount of rows the Entities menu has.
     */
    private static final int ENTITIES_MENU_ROWS = 4;

    /**
     * Setting the amount of pixels the navigation bar scrolls.
     */
    private static final int SCROLL_AMOUNT = 128;

    /**
     * Path to master GamePack.
     */
    private static final String MASTER_GAME_PACK = Paths.get("src/main/resources/master.zip").toAbsolutePath()
            .toString();

    /**
     * Current level loaded in the editor.
     */
    private Level currentLevel;

    /**
     * EditorLogic instance. Does all the editing business logic.
     */
    private EditorLogic editorLogic;

    /**
     * List of UI elements that will be hidden before the user loads or creates a
     * GamePack.
     */
    private List<Node> startupVisibilityList;

    /**
     * List of entries in the entity menu tabs.
     */
    private final List<VBox> entityMenuItems = new ArrayList<>();

    /**
     * Determines if the level is being play tested right now.
     */
    private boolean playTestInProgress = false;

    /**
     * Path to the current Game Pack engine.
     */
    private String archivePath;

    /**
     * Sprite data map, containing its name and another encapsulated map.
     */
    private Map<String, Map<String, Sprite>> spriteData;

    /**
     * Handles all user input from the keyboard.
     */
    KeyEventHandler keyEventHandler;

    /**
     * Root pane of the UI
     */
    @FXML
    public VBox root;

    /**
     * Main canvas instance.
     */
    @FXML
    public Canvas canvas;

    // Upper controls.
    @FXML
    public Button newGamePackButton;
    @FXML
    public Button loadGamePackButton;
    @FXML
    public Button saveGamePackButton;
    @FXML
    public Button quitMenu;
    @FXML
    public MenuBar levelSelectionMenuBar;
    @FXML
    public Menu levelSelectionMenu;
    @FXML
    public MenuItem levelSelectionAdd;
    @FXML
    public MenuItem levelSelectionDelete;
    @FXML
    public Menu backgroundMenu;
    @FXML
    public MenuBar backgroundMenuBar;

    // Middle controls.
    @FXML
    public BorderPane canvasPane;
    @FXML
    public BorderPane levelViewLabel;
    @FXML
    public Label entityTabLabel;
    @FXML
    public TabPane entityMenu;

    // Lower controls.
    @FXML
    public BorderPane controlsPane;
    @FXML
    public BorderPane navigationPane;
    @FXML
    public Button playButton;
    @FXML
    public Button clearLevelButton;
    @FXML
    public Button undoButton;
    @FXML
    public Button redoButton;
    @FXML
    public Button navigationLeftButton;
    @FXML
    public Button navigationRightButton;
    @FXML
    public Button moveToPlayerButton;

    /**
     * JavaFX initialization method. Will be called upon launching the GUI.
     */
    @FXML
    private void initialize() {
        // Done this way to prevent illegal forwarding due to initialization.
        startupVisibilityList = Arrays.asList(saveGamePackButton, levelSelectionMenuBar, clearLevelButton, undoButton,
                redoButton, playButton, navigationLeftButton, navigationRightButton, moveToPlayerButton, entityMenu,
                entityTabLabel, levelViewLabel, backgroundMenuBar);
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        setControlsVisibility(false);
        onInitializeCanvas();
    }

    /**
     * Instantiation of the CanvasController.
     */
    private void onInitializeCanvas() {
        Platform.runLater(() -> { // Necessary, since right after launching the UI root is still null
            waitUntilRootIsInitialized();
            initializeControllersAndModel();
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
        CanvasController canvasController = new CanvasController(canvas);
        keyEventHandler = new KeyEventHandler();
        keyEventHandler.setupActionHandler(root.getScene());
        editorLogic = new EditorLogic(canvasController, keyEventHandler);
        registerHandlers();
        editorLogic.setStackSizes();
    }

    /**
     * Fills individual tabs on the Entity menu with content. Registers callbacks to
     * choose a new current block to place on the level, highlighting it.
     *
     * @param pane      The Pane on the tab on which content is added onto.
     * @param spriteMap List of entities to be displayed on the tab.
     */
    private void initializeEntityTabContent(ScrollPane pane, Map<String, Sprite> spriteMap) {
        GridPane blockGrid = new GridPane();
        blockGrid.setHgap(10);
        blockGrid.setVgap(10);
        blockGrid.setPadding(new Insets(16, 0, 14, 14));
        List<String> backgroundNames = new ArrayList<>();
        List<String> spriteNames = new ArrayList<>();

        for (Map.Entry<String, Sprite> sprite : spriteMap.entrySet()) {
            if (sprite.getValue().getProperties().getEntityType() == EntityType.BACKGROUND) {
                backgroundNames.add(sprite.getKey());
            } else
                spriteNames.add(sprite.getKey());
        }

        Collections.sort(backgroundNames);
        Collections.sort(spriteNames);

        for (int index = 0; index < spriteNames.size(); index += 1) {
            Sprite sprite = spriteMap.get(spriteNames.get(index));
            blockGrid.add(fillTab(sprite), index % ENTITIES_MENU_ROWS, index / ENTITIES_MENU_ROWS);
        }

        for (String backgroundName : backgroundNames) {
            MenuItem backgroundMenuItem = new MenuItem(spriteMap.get(backgroundName).getFancyName());
            backgroundMenuItem.setOnAction(event -> {
                if (!playTestInProgress) {
                    currentLevel.setBackground(spriteMap.get(backgroundName));
                    try {
                        editorLogic.redraw();
                    } catch (InvalidLevelConfiguration e) {
                        MessageHandler.handleException(e);
                    }
                } else
                    MessageHandler.createPlayTestWarningWindow();
            });
            backgroundMenu.getItems().add(backgroundMenuItem);
        }
        pane.setContent(blockGrid);
    }

    /**
     * Helper method to create new entries into a tab on the Entities menu.
     *
     * @param sprite Sprite object whose entry will be added
     * @return Content of individual menu entries.
     */
    private VBox fillTab(Sprite sprite) {
        VBox blockContent = new VBox();
        blockContent.setAlignment(Pos.CENTER);
        blockContent.setMinWidth(115);
        blockContent.setMinHeight(115);
        blockContent.setSpacing(15);
        blockContent.setStyle("-fx-border-color: white; -fx-border-radius: 5; -fx-border-width: 3");
        blockContent.getChildren().addAll(new ImageView(sprite.getImages(Animation.IDLE).get(0)),
                new Label(sprite.getFancyName()));
        entityMenuItems.add(blockContent);
        blockContent.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            clearMenuHighlight();
            setChosenEntity(sprite.getSpriteInformation().getSpriteName(), sprite.getSpriteInformation().getPackName());
            blockContent.setStyle("-fx-border-color: indianred; -fx-border-radius: 5; -fx-border-width: 3");
        });
        return blockContent;
    }

    /**
     * Clears the highlighted (chosen) Entity in the Entity menu.
     */
    private void clearMenuHighlight() {
        for (VBox menuItem : entityMenuItems) {
            menuItem.setStyle("-fx-border-color: white; -fx-border-radius: 5; -fx-border-width: 3");
        }
    }

    /**
     * Creates a new tab on the Entity menu.
     *
     * @param spritePackageName The name of the tab.
     * @param spriteMap         List of entities to be displayed on the tab.
     */
    private void initializeEntityTabs(String spritePackageName, Map<String, Sprite> spriteMap) {
        ScrollPane scrollPaneToAdd = new ScrollPane();
        initializeEntityTabContent(scrollPaneToAdd, spriteMap);
        Tab tabToAdd = new Tab(spritePackageName, scrollPaneToAdd);
        entityMenu.getTabs().add(tabToAdd);
    }

    /**
     * Entry point of the initialization of the Entity menu.
     */
    private void initializeEntityMenu() {
        try {
            entityMenu.getTabs().clear();
            backgroundMenu.getItems().clear();
            Set<String> spritePackageNames = GamePackFactory.getSpritePackNames(archivePath);
            spriteData = GamePackFactory.getSpritePacks(archivePath, spritePackageNames);
            for (String entry : spritePackageNames) {
                initializeEntityTabs(entry, spriteData.get(entry));
            }
        } catch (IOException e) {
            MessageHandler.handleException(e);
        }
    }

    private void setChosenEntity(String spriteName, String spritePackageName) {
        editorLogic.setChosenSprite(spriteData.get(spritePackageName).get(spriteName));
    }

    /**
     * Loads a level into the Editor based on the level path.
     *
     * @param levelPath path to the level inside the Game Pack archive.
     */
    private void loadLevel(String levelPath) {
        try {
            currentLevel = GamePackFactory.getLevel(archivePath, levelPath);
            editorLogic.setCurrentLevel(currentLevel);
            editorLogic.correctCamera();
            editorLogic.redraw();
            setControlsVisibility(true);
        } catch (InvalidLevelConfiguration | IOException e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Quits the application.
     */
    @FXML
    private void quitAction() {
        if (InteractionWindow.ask("Attention!", "Do you really want to quit?")) {
            if (InteractionWindow.ask("Save before quitting?", "Do you want to save before you quit?")) {
                saveGamePackAction();
            }
            ((Stage) root.getScene().getWindow()).close();
        }
    }

    /**
     * Clears the current level.
     */
    @FXML
    public void clearLevelAction() {
        if (InteractionWindow.ask("Are you sure?", "The entire level will be erased!")) {
            try {
                editorLogic.clearLevel();
            } catch (InvalidLevelConfiguration e) {
                MessageHandler.handleException(e);
            }
        }
    }

    /**
     * Creates a new GamePack.
     */
    @FXML
    public void newGamePackAction() {
        if (playTestInProgress) {
            MessageHandler.createPlayTestWarningWindow();
            return;
        }

        FileBrowser fileBrowser = new FileBrowser(root.getScene());

        if (!fileBrowser.saveFile("zip")) {
            return;
        }

        File newGamePack = fileBrowser.getFile();

        String levelName = enterLevelName();

        try {
            archivePath = newGamePack.getPath();
            GamePackFactory.setupArchive(MASTER_GAME_PACK, archivePath,
                    GamePackFactory.getMetanformation(MASTER_GAME_PACK));
            currentLevel = GamePackFactory.getEmptyLevel(archivePath, levelName);

            saveGamePackAction();
            initializeEntityMenu();
            loadLevel(currentLevel.getLevelName());
            refreshLevelList();
            setControlsVisibility(true);
        } catch (IOException | InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Helper method to enter a level name + validating it against the predefined
     * regular expression.
     *
     * @return Valid level name
     */
    private String enterLevelName() {
        Optional<String> levelName = Optional.empty();
        do {
            String messageText = levelName.isEmpty() ? "Enter the name of the level:"
                    : String.format("Invalid level name!%n%nAllowed are:%n%s", LEVEL_NAME_REGEX);
            levelName = InteractionWindow.enter("Level name", messageText);
        } while (levelName.isEmpty() || !LEVEL_NAME_PATTERN.matcher(Objects.requireNonNull(levelName.get())).matches());

        return levelName.get();
    }

    /**
     * Saves the currently opened GamePack and all levels within.
     */
    @FXML
    public void saveGamePackAction() {
        if (playTestInProgress) {
            MessageHandler.createPlayTestWarningWindow();
            return;
        }
        try {
            GamePackFactory.writeLevel(archivePath, currentLevel.getLevelName(), currentLevel);
            MessageHandler.createSavedWindow("Successfully saved to " + archivePath);

        } catch (IOException e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Saves the current level.
     *
     * @throws IOException               In case the GamePack couldn't be found.
     * @throws InvalidLevelConfiguration In case there is an illegal level
     *                                   configuration.
     */
    private void saveCurrentLevel() throws IOException {
        if (InteractionWindow.ask("Save level?", "Do you want to save the current level?")) {
            GamePackFactory.writeLevel(archivePath, currentLevel.getLevelName(), currentLevel);
            MessageHandler.createSavedWindow("Level " + currentLevel.getLevelName() + " successfully saved!");
        }
    }

    /**
     * Loads a GamePack from the drive and initializes it.
     */
    @FXML
    public void loadGamePackAction() {
        FileBrowser fileBrowser = new FileBrowser(root.getScene());

        if (!fileBrowser.openFile("zip")) {
            return;
        }

        try {
            initializeGamePack(fileBrowser.getFileName());
            List<String> levelList = GamePackFactory.getLevelNames(archivePath);
            Optional<Integer> levelToLoad = InteractionWindow.choose(levelList, "Choose level",
                    "Choose the level you want to play:");
            if (levelToLoad.isPresent()) {
                loadLevel(levelList.get(levelToLoad.get()));
                refreshLevelList();
            }
        } catch (IOException e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Initializes GamePack by setting the file path anew, and placing calls to add
     * levels to level drop down menu and filling the Entity menu.
     *
     * @param archivePath Path to the current GamePack.
     * @throws IOException In case the GamePack couldn't be found.
     */
    private void initializeGamePack(String archivePath) throws IOException {
        this.archivePath = archivePath;
        refreshLevelList();
        initializeEntityMenu();
    }

    /**
     * Moves the level view to the left.
     */
    @FXML
    public void navigateLeftAction() {
        try {
            navigateHelper(-SCROLL_AMOUNT);
        } catch (InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Moves the camera to the player.
     */
    @FXML
    public void moveToPlayerAction() {
        if (playTestInProgress) {
            MessageHandler.createPlayTestWarningWindow();
        } else {
            try {
                editorLogic.moveCameraToPlayer();
            } catch (InvalidLevelConfiguration e) {
                MessageHandler.handleException(e);
            }
        }
    }

    /**
     * Moves the level view to the right.
     */
    @FXML
    public void navigateRightAction() {
        try {
            navigateHelper(SCROLL_AMOUNT);
        } catch (InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Helper method for navigation.
     *
     * @param scrollAmount amount of pixels to scroll.
     */
    private void navigateHelper(int scrollAmount) throws InvalidLevelConfiguration {
        if (playTestInProgress) {
            MessageHandler.createPlayTestWarningWindow();
        } else {
            editorLogic.moveCamera(scrollAmount);
        }
    }

    /**
     * Undoes the last action of the user. If nothing is able to be undone, the user
     * will be notified.
     */
    @FXML
    public void undoAction() {
        try {
            editorLogic.undo();
        } catch (InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Redoes the last action of the user. If nothing is able to be redone, the user
     * will be notified.
     */
    @FXML
    public void redoAction() {
        try {
            editorLogic.redo();
        } catch (InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Starts a play test session in the editor window level view.
     */
    @FXML
    public void playAction() {
        // Clear user input
        keyEventHandler.getCurrentlyActiveKeys().clear();

        if (playTestInProgress) {
            try {
                editorLogic.stopPlayTest();
                setMenuVisibilityWhenPlayTesting(true);
            } catch (InvalidLevelConfiguration e) {
                MessageHandler.handleException(e);
            }
        } else {
            try {
                editorLogic.playTest();
                setMenuVisibilityWhenPlayTesting(false);
                setUpPlayTestInterruptListeners();
            } catch (IOException | InvalidLevelConfiguration e) {
                MessageHandler.handleException(e);
            }
        }
        playButton.setText(playTestInProgress ? "Play!" : "Stop!");
        playTestInProgress = !playTestInProgress;
    }

    /**
     * Sets the visibility of menu elements and deactivation of buttons while play
     * testing.
     *
     * @param visibility True for visible, false for invisible
     */
    private void setMenuVisibilityWhenPlayTesting(boolean visibility) {
        levelSelectionMenuBar.setVisible(visibility);
        backgroundMenuBar.setVisible(visibility);
        entityMenu.setVisible(visibility);
        newGamePackButton.setDisable(!visibility);
        loadGamePackButton.setDisable(!visibility);
        saveGamePackButton.setDisable(!visibility);
        undoButton.setDisable(!visibility);
        redoButton.setDisable(!visibility);
        clearLevelButton.setDisable(!visibility);
        navigationLeftButton.setDisable(!visibility);
        navigationRightButton.setDisable(!visibility);
        moveToPlayerButton.setDisable(!visibility);
    }

    /**
     * Sets up listeners to determine if the player won the level. If so, the play
     * test will be interrupted and editing mode will be re-activated.
     */
    private void setUpPlayTestInterruptListeners() {
        editorLogic.getGameLoop().getLevelWasWonProperty()
                .addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
                    if (newValue.booleanValue()) {
                        interruptPlayTest();
                        InteractionWindow.inform("You beat the level!", "Now back to editing!");
                    }
                }));
        editorLogic.getGameLoop().getLives().addListener((observable, oldValue, newValue) -> Platform.runLater(() -> {
            interruptPlayTest();
            InteractionWindow.inform("You died!", "Now back to editing!");
        }));
    }

    /**
     * Interrupts the play test session if the player has been defeated or won the
     * level.
     */
    private void interruptPlayTest() {
        try {
            editorLogic.stopPlayTest();
            setMenuVisibilityWhenPlayTesting(true);
            playTestInProgress = false;
            playButton.setText("Play!");
        } catch (InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Adds a new level to the Game Pack.
     */
    @FXML
    public void levelSelectionAddAction() {
        String levelName = enterLevelName();
        try {
            Level newLevel = GamePackFactory.getEmptyLevel(archivePath, levelName);
            GamePackFactory.writeLevel(archivePath, newLevel.getLevelName(), newLevel);
            refreshLevelList();
        } catch (IOException | InvalidLevelConfiguration e) {
            MessageHandler.handleException(e);
        }
    }

    /**
     * Deletes a level in the Level Selection menu.
     */
    @FXML
    public void levelSelectionDeleteAction() {
        if (levelSelectionMenu.getItems().size() == PROTECTED_ENTRIES_LEVEL_MENU) {
            MessageHandler.createWarningWindow("You can't delete the only level in this GamePack!");
        } else {
            try {
                deleteLevel();
                refreshLevelList();
            } catch (IOException e) {
                MessageHandler.handleException(e);
            }
        }
    }

    /**
     * Removes the level from the GamePack.
     *
     * @throws IOException In case the removal process results in an illegal level
     *                     configuration.
     */
    private void deleteLevel() throws IOException {
        List<String> levelList = GamePackFactory.getLevelNames(archivePath);
        Optional<Integer> levelToDelete = InteractionWindow.choose(levelList, "Delete Level",
                "Choose which level to delete:");
        if (levelToDelete.isPresent()) {
            if (!currentLevel.getLevelName().equals(levelList.get(levelToDelete.get()))) {
                GamePackFactory.removeLevelData(archivePath, levelList.get(levelToDelete.get()));
                refreshLevelList();
            } else {
                MessageHandler.createErrorWindow("You can't delete the level you're editing right now!");
            }
        }

    }

    /**
     * Helper method to disable menu controls if an archive isn't loaded yet.
     *
     * @param visibility true for visible, false for invisible
     */
    private void setControlsVisibility(boolean visibility) {
        for (Node node : startupVisibilityList) {
            node.setVisible(visibility);
        }
    }

    /**
     * Adds levels to the level drop down menu.
     *
     * @throws IOException In case there is an error while reading from the
     *                     GamePack.
     */
    private void refreshLevelList() throws IOException {
        clearLevelMenu();
        List<String> levelList = GamePackFactory.getLevelNames(archivePath);
        for (String entry : levelList) {
            MenuItem itemToAdd = new MenuItem(entry);
            itemToAdd.setOnAction(event -> {
                try {
                    saveCurrentLevel();
                    loadLevel(entry);
                } catch (IOException e) {
                    MessageHandler.handleException(e);
                }
            });
            levelSelectionMenu.getItems().add(itemToAdd);
        }
    }

    /**
     * Helper method to clear the level selection menu of all entries to be rebuilt
     * anew.
     */
    private void clearLevelMenu() {
        if (levelSelectionMenu.getItems().size() > PROTECTED_ENTRIES_LEVEL_MENU) {
            levelSelectionMenu.getItems().remove(PROTECTED_ENTRIES_LEVEL_MENU, levelSelectionMenu.getItems().size());
        }
    }

    /**
     * Registers listeners on the undo / redo action stack. Deactivates or activates
     * the buttons depending on whether there are actions to be redone or undone.
     */
    private void registerHandlers() {
        editorLogic.getRedoStackSizeProperty()
                .addListener((observable, oldValue, newValue) -> redoButton.setDisable(newValue.intValue() == 0));
        editorLogic.getUndoStackSizeProperty()
                .addListener((observable, oldValue, newValue) -> undoButton.setDisable(newValue.intValue() == 0));
    }

}