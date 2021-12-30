package ch.zhaw.pm3.teamretro.logic.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.Level;
import ch.zhaw.pm3.teamretro.gamepack.entity.Enemy;
import ch.zhaw.pm3.teamretro.gamepack.entity.Entity;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;
import ch.zhaw.pm3.teamretro.gamepack.entity.Position;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;
import ch.zhaw.pm3.teamretro.logic.common.CanvasController;
import ch.zhaw.pm3.teamretro.logic.common.KeyEventHandler;
import ch.zhaw.pm3.teamretro.logic.common.RenderEngine;
import ch.zhaw.pm3.teamretro.logic.game.GameLoop;
import ch.zhaw.pm3.teamretro.ui.common.MessageHandler;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.MouseEvent;
import javafx.util.Pair;

/**
 * Main editor model / logic loop class. Does all the editor magic, makes calls
 * to place Entities on the canvas and computing inputs from the user, passed
 * from the EditorController.
 */
public class EditorLogic {
    /**
     * Standard Y Offset
     */
    private static final int STANDARD_Y_OFFSET = 0;

    /**
     * Determines if a play test is in progress.
     */
    private boolean playTestInProgress = false;

    /**
     * Invalid state in undo/redo order error message.
     */
    private static final String INVALID_ACTION_STACK_STATE = "Invalid state in undo/redo order.";

    /**
     * CanvasController, controls the canvas.
     */
    private final CanvasController canvasController;

    /**
     * Key handler. Handles the inputs by the user.
     */
    private final KeyEventHandler keyEventHandler;

    /**
     * RenderEngine instance.
     */
    private final RenderEngine renderEngine;

    /**
     * Currently chosen Entity. This is what the player chooses in the Editor to
     * draw on the canvas.
     */
    private Sprite chosenSprite;

    /**
     * Currently chosen level. Gets passed by EditorController
     */
    private Level currentLevel;

    /**
     * Game Loop instance. Needed for play testing.
     */
    private GameLoop gameLoop;

    /**
     * Stack of undo/redo actions and objects. First element of pair is the added
     * Entity, second element is the removed Entity, if available.
     * <p>
     * In the pair the first optional entity represents the newly created entity
     * drawn to the screen. The second optional entity represents the removed entity
     * from any given position, during that given action.
     */
    private ActionStack<PlacementAction, List<Pair<Optional<Entity>, Optional<Entity>>>> actionStack = new ActionStack<>();

    /**
     * Will temporarily save the last set entity, so that assuming the same entity
     * is set onto the the same position, it is not pushed onto the stack.
     * <p>
     * Starts as an empty optional, to not have to deal with null.
     * <p>
     * Lint will say that using an Optional as a field is suboptimal, but there is
     * no efficient workaround since we start with an empty value and we want to
     * avoid using null.
     */
    private Optional<Pair<Sprite, Position>> lastSavedEntity = Optional.empty();

    /**
     * Will temporarily save the last deleted position, so to not have a double
     * entry on the stack.
     * <p>
     * Lint will say that using an Optional as a field is suboptimal, but there is
     * no efficient workaround since we start with an empty value and we want to
     * avoid using null.
     */
    private Optional<Position> lastDeletedPosition = Optional.empty();

    /**
     * Size of redo ActionStack. Needed for UI purposes, UI will add a
     * ChangeListener.
     */
    private final IntegerProperty redoStackSize = new SimpleIntegerProperty(0);

    /**
     * Size of undo ActionStack. Needed for UI purposes, UI will add a
     * ChangeListener.
     */
    private final IntegerProperty undoStackSize = new SimpleIntegerProperty(0);

    /**
     * Constructor of the editor logic.
     *
     * @param canvasController Controlling abstraction class that will handle the
     *                         canvas on the Editor UI.
     * @param keyEventHandler  Keyboard event handler that will be registered on the
     *                         main window.
     */
    public EditorLogic(CanvasController canvasController, KeyEventHandler keyEventHandler) {
        this.canvasController = canvasController;
        this.keyEventHandler = keyEventHandler;
        addEventHandlers();
        renderEngine = new RenderEngine();
        renderEngine.setCanvasController(canvasController);
    }

    /**
     * Adding EventHandlers to the canvas. Defines behavior for single clicks and
     * held-down clicks (so-called 'drawing mode')
     */
    private void addEventHandlers() {
        // Single clicks
        canvasController.getCanvas().setOnMouseClicked(event -> {
            try {
                mouseClickHandler(event);
            } catch (InvalidLevelConfiguration e) {
                MessageHandler.handleException(e);
            }
        });
        // Clicking and dragging ('painting' mode)
        canvasController.getCanvas().setOnMouseDragged(event -> {
            try {
                mouseClickHandler(event);
            } catch (InvalidLevelConfiguration e) {
                MessageHandler.handleException(e);
            }
        });
    }

    /**
     * Chooses the current Entity to be drawn on the canvas.
     *
     * @param sprite The sprite chosen by the user.
     */
    public void setChosenSprite(Sprite sprite) {
        chosenSprite = sprite;
    }

    /**
     * Places the entity chosen by the player (called by setChosenEntity) on the
     * level.
     *
     * @param x x position.
     * @param y y position.
     */
    public void addEntityOnLevel(double x, double y) throws InvalidLevelConfiguration {
        if (chosenSprite == null) {
            MessageHandler.createWarningWindow("No sprite chosen!");
            return;
        }
        Position nextPosition = calculatePosition(x, y);
        Sprite nextSprite = chosenSprite;

        // check if I override the currently last deleted position, if so remove it
        if (lastDeletedPosition.isPresent() && lastDeletedPosition.get().equals(nextPosition)) {
            lastDeletedPosition = Optional.empty();
        }

        // check if null per default and if there are any repetitive values sent.
        if (lastSavedEntity.isPresent() && lastSavedEntity.get().getKey().equals(nextSprite)
                && lastSavedEntity.get().getValue().equals(nextPosition)) {
            return;
        }

        // set the last sent entity
        lastSavedEntity = Optional.of(new Pair<>(nextSprite, nextPosition));

        Pair<Optional<Entity>, Optional<Entity>> temp = currentLevel
                .addEntity(chosenSprite.getProperties().getEntityType(), chosenSprite, nextPosition);

        actionStack.push(PlacementAction.ADD, Collections.singletonList(temp));
        setStackSizes();
        redraw();
    }

    /**
     * Removes entity chosen by the player (called by setChosenEntity) on the level
     * using x and y coordinates passed by mouse click.
     *
     * @param x x position.
     * @param y y position.
     */
    public void removeEntityFromLevel(double x, double y) throws InvalidLevelConfiguration {
        if (chosenSprite == null) {
            MessageHandler.createWarningWindow("No sprite chosen!");
            return;
        }

        Position clickedPosition = calculatePosition(x, y);

        // Check if I am removing the last set entity from the board, then remove the
        // current entry
        if (lastSavedEntity.isPresent() && lastSavedEntity.get().getValue().equals(clickedPosition)) {
            lastSavedEntity = Optional.empty();
        }

        // Check if I clicked that position the last time I did anything.
        if (lastDeletedPosition.isPresent() && lastDeletedPosition.get().equals(clickedPosition)) {
            return;
        }

        lastDeletedPosition = Optional.of(clickedPosition);
        Optional<Entity> removedEntity = currentLevel.removeEntity(clickedPosition);

        if (removedEntity.isPresent()) {
            actionStack.push(PlacementAction.REMOVE,
                    Collections.singletonList(new Pair<>(Optional.empty(), removedEntity)));
        }
        setStackSizes();
        redraw();
    }

    /**
     * Sets the current level to this logic class and to the RenderEngine.
     *
     * @param currentLevel level that shall be loaded, passed by the
     *                     EditorController.
     */
    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
        actionStack = new ActionStack<>();
        renderEngine.setCurrentLevel(currentLevel);
    }

    /**
     * Redraws the level.
     */
    public void redraw() throws InvalidLevelConfiguration {
        renderEngine.render();
    }

    /**
     * Helper method to convert the x and y coordinates to multiples of 32.
     *
     * @param x exact x position
     * @param y exact y position
     * @return Position object with positions calculated with multiples of 32.
     */
    private Position calculatePosition(double x, double y) {
        double xCorrected = (((int) x >> 5) << 5) - canvasController.getCamOffset().getX();
        double yCorrected = (((int) y >> 5) << 5) - canvasController.getCamOffset().getY();
        return new Position(xCorrected, yCorrected);
    }

    /**
     * Helper method for click actions.
     *
     * @param event Mouse event
     */
    private void mouseClickHandler(MouseEvent event) throws InvalidLevelConfiguration {
        switch (event.getButton()) {
            case PRIMARY:
                if (!playTestInProgress)
                    addEntityOnLevel(event.getX(), event.getY());
                break;
            case SECONDARY:
                if (!playTestInProgress)
                    removeEntityFromLevel(event.getX(), event.getY());
                break;
            default:
                break;
        }
    }

    /**
     * Clears the level by removing all entities.
     */
    public void clearLevel() throws InvalidLevelConfiguration {

        List<Pair<Optional<Entity>, Optional<Entity>>> entityList = new ArrayList<>();
        for (Entity entity : currentLevel.getEntityList()) {
            if (entity.getEntityType() == EntityType.PLAYER) {
                continue;
            }

            Optional<Entity> removedEntity = currentLevel.removeEntity(entity.getPosition());

            if (removedEntity.isPresent()) {
                entityList.add(new Pair<>(Optional.empty(), removedEntity));
            }
        }
        actionStack.push(PlacementAction.REMOVE, entityList);
        redraw();
    }

    /**
     * Moves the camera.
     *
     * @param amount Positive for right, negative for left
     */
    public void moveCamera(int amount) throws InvalidLevelConfiguration {
        canvasController.moveCamera(amount);
        redraw();
    }

    /**
     * Commences a play test session.
     */
    public void playTest() throws IOException, InvalidLevelConfiguration {
        canvasController.setCamOffset(currentLevel.getPlayer().getPosition().getX(), STANDARD_Y_OFFSET);
        currentLevel.getPlayer().setVelocity(0, 0);
        currentLevel.getPlayer().setSpeed(0);
        gameLoop = new GameLoop(canvasController, keyEventHandler);
        gameLoop.setCurrentLevel(currentLevel);
        gameLoop.setKeyEventHandler(keyEventHandler);
        gameLoop.run(true);
        playTestInProgress = true;
    }

    /**
     * Terminates a play test session.
     */
    public void stopPlayTest() throws InvalidLevelConfiguration {
        gameLoop.stop();
        // gameLoop.getLevelWasWonProperty().set(false); not sure if necessary
        currentLevel.getPlayer().setVelocity(0, 0);
        currentLevel.getPlayer().setSpeed(0);
        playTestInProgress = false;
        canvasController.setCamOffset(((int) canvasController.getCamOffset().getX() >> 5) << 5, STANDARD_Y_OFFSET);
        resetEnemiesToSpawnPosition();
        redraw();
    }

    /**
     * Resets all enemies to their original spawn position.
     */
    private void resetEnemiesToSpawnPosition() {
        for (Enemy enemy : currentLevel.getEnemyList().values()) {
            enemy.setPosition(enemy.getSpawnPosition());
        }
    }

    /**
     * Undoes the last action the player has made using the editor.
     *
     * @throws InvalidLevelConfiguration In case the operation results into an
     *                                   illegal level configuration.
     */
    public void undo() throws InvalidLevelConfiguration {
        Optional<ActionStack.Node<PlacementAction, List<Pair<Optional<Entity>, Optional<Entity>>>>> node = actionStack
                .undo();

        if (node.isEmpty()) {
            return;
        }

        PlacementAction action = node.get().getAction();

        for (Pair<Optional<Entity>, Optional<Entity>> pair : node.get().getValue()) {
            if (action == PlacementAction.ADD) {
                if (pair.getKey().isEmpty()) {
                    throw new InvalidLevelConfiguration(INVALID_ACTION_STACK_STATE);
                }
                if (pair.getValue().isPresent()) { // removing is not needed, as during adding previous entity is
                                                   // removed
                    Entity redoneEntity = pair.getValue().get();
                    currentLevel.addEntity(redoneEntity.getEntityType(), redoneEntity.getSprite(),
                            redoneEntity.getPosition());
                } else {
                    Entity undoneEntity = pair.getKey().get();
                    currentLevel.removeEntity(undoneEntity.getPosition());
                }
            } else if (action == PlacementAction.REMOVE) {
                if (pair.getValue().isEmpty())
                    throw new InvalidLevelConfiguration(INVALID_ACTION_STACK_STATE);
                Entity undoneEntity = pair.getValue().get();
                currentLevel.addEntity(undoneEntity.getEntityType(), undoneEntity.getSprite(),
                        undoneEntity.getPosition());
            }
        }
        setStackSizes();
        redraw();
    }

    /**
     * Redoes the last action the player has made using the editor.
     *
     * @throws InvalidLevelConfiguration In case the operation results into an
     *                                   illegal level configuration.
     */
    public void redo() throws InvalidLevelConfiguration {
        Optional<ActionStack.Node<PlacementAction, List<Pair<Optional<Entity>, Optional<Entity>>>>> node = actionStack
                .redo();

        if (node.isEmpty()) {
            return;
        }

        PlacementAction action = node.get().getAction();

        // new old
        for (Pair<Optional<Entity>, Optional<Entity>> pair : node.get().getValue()) {
            if (action == PlacementAction.ADD) {
                if (pair.getKey().isEmpty()) {
                    throw new InvalidLevelConfiguration(INVALID_ACTION_STACK_STATE);
                }

                Entity undoneEntity = pair.getKey().get();
                currentLevel.addEntity(undoneEntity.getEntityType(), undoneEntity.getSprite(),
                        undoneEntity.getPosition());

            } else if (action == PlacementAction.REMOVE) {
                if (pair.getValue().isEmpty()) {
                    throw new InvalidLevelConfiguration(INVALID_ACTION_STACK_STATE);
                }
                Entity undoneEntity = pair.getValue().get();
                // there is only a need to add the "new" element, as the add entity functions
                // removes any previously added on its own.
                currentLevel.removeEntity(undoneEntity.getPosition());
            }
        }
        setStackSizes();
        redraw();
    }

    /**
     * Moves the camera to the protagonist.
     */
    public void moveCameraToPlayer() throws InvalidLevelConfiguration {
        canvasController.setCamOffset(
                -currentLevel.getPlayer().getPosition().getX() + canvasController.getCanvas().getWidth() / 2,
                STANDARD_Y_OFFSET);
        redraw();
    }

    /**
     * Corrects the camera offset to a multiple of 32 to ensure building of levels
     * will happen in a correct manner.
     */
    public void correctCamera() {
        canvasController.setCamOffset(((int) canvasController.getCamOffset().getX() >> 5) << 5, STANDARD_Y_OFFSET);
    }

    /**
     * Updating stack size IntegerProperties with the size of the ActionStacks.
     * Needed for UI purposes.
     */
    public void setStackSizes() {
        undoStackSize.set(actionStack.getUndoStackSize());
        redoStackSize.set(actionStack.getRedoStackSize());
    }

    public IntegerProperty getRedoStackSizeProperty() {
        return redoStackSize;
    }

    public IntegerProperty getUndoStackSizeProperty() {
        return undoStackSize;
    }

    public GameLoop getGameLoop() {
        return gameLoop;
    }
}
