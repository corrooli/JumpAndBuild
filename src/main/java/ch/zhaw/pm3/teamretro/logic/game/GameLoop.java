package ch.zhaw.pm3.teamretro.logic.game;

import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ch.zhaw.pm3.teamretro.gamepack.GamePackFactory;
import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.Level;
import ch.zhaw.pm3.teamretro.gamepack.entity.Direction;
import ch.zhaw.pm3.teamretro.gamepack.entity.Enemy;
import ch.zhaw.pm3.teamretro.gamepack.entity.Entity;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;
import ch.zhaw.pm3.teamretro.gamepack.entity.MovingEntity;
import ch.zhaw.pm3.teamretro.gamepack.entity.Player;
import ch.zhaw.pm3.teamretro.gamepack.entity.Position;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Animation;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Behavior;
import ch.zhaw.pm3.teamretro.logic.common.CanvasController;
import ch.zhaw.pm3.teamretro.logic.common.KeyEventHandler;
import ch.zhaw.pm3.teamretro.logic.common.RenderEngine;
import ch.zhaw.pm3.teamretro.ui.common.MessageHandler;
import ch.zhaw.pm3.teamretro.ui.game.controller.GameController;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.shape.Rectangle;

/**
 * Houses and instantiates the game loop, so the main flow of the program.
 * Timing is handled by another class called TimedLoop with which we override
 * runInnerLoop() in run().
 * 
 */
public class GameLoop {

    /**
     * Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger(GameLoop.class.getName());

    /**
     * Default Y axis camera offset. It's currently at 0, denoting that the camera
     * can not move up or down at all and is locked horizontally.
     */
    private static final double DEFAULT_Y_OFFSET = 0;

    /**
     * In collision detection, we define the amount of blocks around the player we
     * want to check.
     */
    private static final int MOVING_ENTITY_SURROUNDING_BLOCK_COUNT = 10;

    /**
     * What the height and width dimensions of a block are
     */
    private static final int BLOCK_SIZE = 32;

    /**
     * Important to stop the game from spazzing out right after starting the
     * application
     */
    private static final double DELTA_CAP = 100;

    /**
     * By how much the delta time is multiplied each frame. Effectively defines the
     * game speed.
     */
    private static final double DELTA_MULTIPLIER = 150;

    /**
     * definition of the left scroll margin (20% of the screen)
     */
    private static final double PERCENT_LEFT_BORDER = 20.0;

    /**
     * definition of the right scroll margin (70% of the screen)
     */
    private static final double PERCENT_RIGHT_BORDER = 70.0;

    /**
     * definition of the full scroll margin (100% of the screen)
     */
    private static final double PERCENT_FULL_BORDER = 100.0;

    /**
     * Amount of lives the player starts out with.
     */
    private static final int STARTING_LIVES = 3;

    /**
     * Score the player starts out with.
     */
    private static final int STARTING_SCORE = 0;

    /**
     * Current score the player has. The idea is to increase it when the player
     * kills an enemy. It's needed as property so the UI classes can listen for it
     * and display updates immediately.
     */
    private final IntegerProperty score = new SimpleIntegerProperty(STARTING_SCORE);

    /**
     * Current number of lives the player has. When the player comes in contact with
     * a spike, enemy or falls outside of the level, his lives decrease.
     */
    private final IntegerProperty lives = new SimpleIntegerProperty(STARTING_LIVES);

    /**
     * Gets set to true if the level was won. Important for the GameController class
     * to handle moving to the next level.
     */
    private final BooleanProperty levelWasWon = new SimpleBooleanProperty();

    /**
     * Canvas controller: helps drawing objects on the screen. Takes care of camera
     * offsets as well.
     */
    private final CanvasController canvasController;

    /**
     * Left border. If crossed, level begins to scroll.
     */
    private final double scrollLeftBorder;

    /**
     * Right border. If crossed, level begins to scroll.
     */
    private final double scrollRightBorder;

    /**
     * <p>
     * Render engine, helps drawing objects on the screen.
     * </p>
     */
    private final RenderEngine renderer;

    /**
     * Simple interface to check for keyboard input.
     */
    private KeyEventHandler keyEventHandler;

    /**
     * Inner gameloop abstract method which we override in run().
     */
    private TimedLoop loop;

    /**
     * <p>
     * Absolute path to game pack
     * </p>
     */
    private String packPath;

    /**
     * <p>
     * Name of the level. If we live load, then we can load this ourselves using
     * GamePackFactory.
     * </p>
     * 
     * @see GameLoop#run(boolean)
     */
    private String levelName;

    /**
     * <p>
     * Reference of the Player object (which we get from the Level instance form the
     * GamePackFactory).
     * </p>
     */
    private Player plr;

    /**
     * <p>
     * Reference to the current level (we get either from outside or load ourselves
     * with GamePackFactory).
     * </p>
     * 
     * @see GameLoop#run(boolean)
     */
    private Level currentLevel;

    /**
     * <p>
     * List of currently killed enemies during runtime. The idea to restore them
     * when we restart the level
     * </p>
     * 
     * @see Level#restoreEnemies(List)
     */
    private List<Enemy> killedEnemies;

    /**
     * <p>
     * Delta for any kind of movement on screen
     * </p>
     * 
     * @see <a href="https://en.wikipedia.org/wiki/Delta_timing">Delta Timing</a>
     */
    private double delta;

    /**
     * to let framerate stabilize
     */
    private boolean warmUp;

    /**
     * <p>
     * GameLoop requires already instantiated CanvasController and KeyEventHandler
     * objects.
     * </p>
     * 
     * @param canvasController For camera and drawing images
     * @param keyEventHandler  Reading keyboard inputs
     */
    public GameLoop(CanvasController canvasController, KeyEventHandler keyEventHandler) {
        this.canvasController = canvasController;
        this.keyEventHandler = keyEventHandler;

        scrollLeftBorder = canvasController.getCanvas().getWidth() / PERCENT_FULL_BORDER * PERCENT_LEFT_BORDER;
        scrollRightBorder = canvasController.getCanvas().getWidth() / PERCENT_FULL_BORDER * PERCENT_RIGHT_BORDER;

        renderer = new RenderEngine();
        renderer.setCanvasController(canvasController);
    }

    /**
     * <p>
     * When we start a new level we don't want that the enemies killed in a previous
     * level get restored, that's why we clear the current array with killed enemies
     * </p>
     * 
     * @see GameLoop#preRunSetup(boolean)
     * @see GameLoop#restartLevel()
     */
    private void clearKilledEnemies() {
        killedEnemies = Collections.synchronizedList(new ArrayList<Enemy>());
        killedEnemies.clear();
    }

    /**
     * <p>
     * Creates a clean slate when a level starts (a new TimedLoop is instantiated).
     * </p>
     * 
     * @param liveLoad If true, then an outside class such as in the level editor
     *                 does the loading for us. If false, then we load the level
     *                 ourselves using GamePackFactory.
     * @see GamePackFactory#getLevel(String, String)
     * @see GameController#runGameLoop()
     */
    private void preRunSetup(boolean liveLoad) throws IOException, InvalidLevelConfiguration {
        warmUp = true; // grace time for the framerate to stabilize

        // carry level and score from last level or initial state
        score.set(score.get());
        lives.set(lives.get());

        if (!liveLoad && !(packPath == null || levelName == null)) {
            currentLevel = GamePackFactory.getLevel(packPath, levelName);
        }

        // Reset input and player velocity
        currentLevel.getPlayer().setVelocity(0, 0);
        keyEventHandler.getCurrentlyActiveKeys().clear();

        renderer.setCurrentLevel(currentLevel);

        clearKilledEnemies();

        plr = currentLevel.getPlayer();

        resetCamToPlayer();
    }

    /**
     * <p>
     * The game loop entry point.
     * </p>
     *
     * @param liveLoad setCurrentLevel() must be called before (important for
     *                 Editor)
     * @see TimedLoop#runInnerLoop(double, boolean)
     */
    public void run(boolean liveLoad) throws IOException, InvalidLevelConfiguration {
        preRunSetup(liveLoad);

        loop = new TimedLoop() {
            @Override
            public void runInnerLoop(double lastFrameTime, boolean secHappend) {
                try {

                    if (!warmUp) { // once warmup's over we run the game as usual
                        delta = DELTA_MULTIPLIER * lastFrameTime;
                    } else {
                        // no movement until warmup / stabilization of framerate
                        delta = 0;
                        lastFrameTime = 0;
                    }

                    movableEntitiesTick(lastFrameTime);

                    renderer.render();

                    if (secHappend) {
                        handleFallenEntities();
                        if (warmUp) {
                            prepCamOffset();
                        }
                        warmUp = false; // warmup done after a second
                    }

                } catch (InvalidLevelConfiguration e) {
                    LOGGER.log(SEVERE, "Game pack configuration was invalid (Player). Exiting.");
                    stop();
                    Platform.exit();
                }
            }
        };

        loop.start();
    }

    /**
     * <p>
     * All the movement regarding the player, enemies and scrolling around him
     * happens here.
     * </p>
     *
     * @param lastFrameTime
     * @throws InvalidLevelConfiguration
     */
    private void movableEntitiesTick(double lastFrameTime) throws InvalidLevelConfiguration {
        // bool to know whether it's time to cycle the player walk animation!
        boolean cycleWalkAnim = plr.tickWalkAnimations(lastFrameTime);

        plr.setGravity(Math.min(10.0 * lastFrameTime, 1));
        for (Enemy e : currentLevel.getEnemyList().values()) {
            e.setGravity(Math.min(10.0 * lastFrameTime, 1));
            boolean cycle = e.tickWalkAnimations(lastFrameTime);
            if (cycle) {
                e.getNextImage(Animation.WALK);
            }
        }

        checkForCollision(plr);

        for (Enemy e : currentLevel.getEnemyList().values()) {
            e.setVelocity(e.getWalkDirection() == Direction.LEFT ? -Math.min(delta / 2, DELTA_CAP)
                    : Math.min(delta / 2, DELTA_CAP), e.getVelocity().getY());
            e.setFlipped(e.getWalkDirection() != Direction.LEFT);

            checkForCollision(e);

            if (cycleWalkAnim) {
                plr.getNextImage(Animation.WALK);
            }
        }

        handleKeyPresses(cycleWalkAnim);

        // Scroll right
        if (plr.getPosition().getX() + canvasController.getCamOffset().getX() >= scrollRightBorder) {
            canvasController.moveCamera(Math.min(delta, DELTA_CAP));
        }
        // Scroll left
        if (plr.getPosition().getX() + canvasController.getCamOffset().getX() <= scrollLeftBorder) {
            canvasController.moveCamera(-Math.min(delta, DELTA_CAP));
        }
    }

    /**
     * <p>
     * Handle what happens when certain keys are pressed, for movements and so on.
     * </p>
     * 
     * @throws InvalidLevelConfiguration
     */
    private void handleKeyPresses(boolean cycleWalkAnim) throws InvalidLevelConfiguration {
        // Jump
        if (plr.isOnGround()) {
            if (isKeyPressed(KeyBindings.JUMP)) {
                plr.jump();
                // set jump animation
                plr.setOnGround(false);

            } else if (plr.getVelocity().getY() > 0) {
                plr.setOnGround(true);
                plr.setColliding(false);
            } else {
                if (cycleWalkAnim) {
                    plr.getNextImage(Animation.WALK);
                }
            }
        } else {
            plr.getNextImage(Animation.JUMP);
        }

        // Speed boost
        if (isKeyPressed(KeyBindings.RUN)) {
            delta *= 2;
            plr.setWalkStyle(true);
        } else {
            plr.setWalkStyle(false);
        }

        if (isKeyPressed(KeyBindings.WALK_RIGHT)) {
            // Right
            plr.setFlipped(true);
            plr.setVelocity(delta, plr.getVelocity().getY());
            if (plr.isOnGround() && cycleWalkAnim) {
                plr.getNextImage(Animation.WALK);
            }
        } else if (isKeyPressed(KeyBindings.WALK_LEFT)) {
            // Left
            plr.setFlipped(false);
            plr.setVelocity(-delta, plr.getVelocity().getY());
            if (plr.isOnGround() && cycleWalkAnim) {
                plr.getNextImage(Animation.WALK);
            }
        } else {
            plr.setVelocity(0, plr.getVelocity().getY());
            if (plr.isOnGround()) {
                plr.getNextImage(Animation.IDLE);
            }
        }
    }

    /**
     * <p>
     * Checks if a key is pressed.
     * </p>
     *
     * @param keyBindings Enum object containing an array of applicable KeyCodes.
     * @return True if key was pressed, false if it wasn't.
     */
    private boolean isKeyPressed(KeyBindings keyBindings) {
        for (KeyCode keyCode : keyBindings.getKeyCodes()) {
            if (keyEventHandler.getCurrentlyActiveKeys().contains(keyCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Reference: This method very roughly is based upon the following source:
     * <a href=
     * "https://github.com/OneLoneCoder/videos/blob/master/OneLoneCoder_PlatformGame1.cpp#L219">OneLoneCoder
     * Platformer example</a>
     * </p>
     *
     * @throws InvalidLevelConfiguration
     */
    private void checkForCollision(MovingEntity subject) throws InvalidLevelConfiguration {
        Direction collisionX = Direction.NONE;
        Direction collisionY = Direction.NONE;

        // Map with collidables contains entities which can be collided with
        // Providing a Position gives you the entity which occupies that place
        // (clamped to 32x32).
        // The map contains the enemies, blocks, and the player itself.
        Map<Position, Entity> mapWithCollidables = new ConcurrentHashMap<>();
        for (Entity ent : currentLevel.getBlockList().values()) {
            if (ent.getProperties().isSolid() || ent.getProperties().getBehavior() == Behavior.WIN) {
                mapWithCollidables.put(ent.getPosition(), ent);
            }
        }
        for (Enemy e : currentLevel.getEnemyList().values()) {
            mapWithCollidables.put(e.getPosition().clamp(), e);
        }
        mapWithCollidables.put(plr.getPosition().clamp(), plr);
        // End: filling up mapWithCollidables

        Entity[] collEnts = getBlockAtPosOfPlr(mapWithCollidables, subject);

        // entity we collided with, can of course be air, so no block at all, thus
        // optional:
        Optional<Entity> target = Optional.empty();

        // Here we get the theoretical next position and bounding box after the
        // next tick.
        Rectangle newSubjectRect = subject.getBoundingBoxAfterTick();

        if (subject.getVelocity().getY() < 0) { // jumping (v.y < 0)
            // Entities 0 1 2
            for (int i = 0; i <= 2; i++) {
                if (collEnts[i] != null && collEnts[i].getBoundingBox().intersects(newSubjectRect.getBoundsInLocal())) {
                    // Collides with block i after tick!
                    collisionY = Direction.UP;
                    target = Optional.of(collEnts[i]);
                    break;
                }
            }
        } else if (subject.getVelocity().getY() > 0) { // falling (v.y > 0)
            // Entities 5 6 7
            for (int i = 5; i <= 7; i++) {
                if (collEnts[i] != null && collEnts[i].getBoundingBox().intersects(newSubjectRect.getBoundsInLocal())) {
                    // Collides with block i after tick!
                    collisionY = Direction.DOWN;
                    target = Optional.of(collEnts[i]);
                    break;
                }
            }
        }

        if (subject.getVelocity().getX() < 0) { // left (v.x < 0)
            // Entities 8-9
            for (int i = 8; i <= 9; i++) {
                if (collEnts[i] != null && collEnts[i].getBoundingBox().intersects(newSubjectRect.getBoundsInLocal())) {
                    // Collides with block i after tick!
                    collisionX = Direction.LEFT;
                    target = Optional.of(collEnts[i]);
                    break;
                }
            }
        } else if (subject.getVelocity().getX() > 0) { // right (v.x > 0)
            // Entities 3-4
            for (int i = 3; i <= 4; i++) {
                if (collEnts[i] != null && collEnts[i].getBoundingBox().intersects(newSubjectRect.getBoundsInLocal())) {
                    // Collides with block i after tick!
                    collisionX = Direction.RIGHT;
                    target = Optional.of(collEnts[i]);
                    break;
                }
            }
        }

        handleCollisionResult(subject, target, collisionX, collisionY);
    }

    /**
     * <p>
     * We now concretely implement what happens when entities of certain types and
     * behaviors collide with each other.
     * </p>
     * 
     * @param subject    From which subject's perspective we see the collision with.
     * @param target     The thing we're colliding with.
     * @param collisionX In which direction we're colliding horizontally.
     * @param collisionY In which direction we're colliding vertically.
     */
    private void handleCollisionResult(MovingEntity subject, Optional<Entity> target, Direction collisionX,
            Direction collisionY) {
        if (target.isEmpty()) { // if we collided with any entity
            subject.tick(collisionX, collisionY);
            return;
        }

        if (!target.get().getProperties().isSolid()) {
            // no collision when not solid, such as clouds
            collisionY = Direction.NONE;
            collisionX = Direction.NONE;
        }

        Behavior targetBehavior = target.get().getProperties().getBehavior();

        switch (targetBehavior) {
            case STATIC:
                subject.tick(collisionX, collisionY);
                break;
            case PLAYABLE: // using fallthrough
            case GENERICENEMY:
                handleCollisionWithEnemy(subject, target, collisionX, collisionY);
                break;
            case DAMAGE:
                handleDamageBlocks(subject, collisionY);
                break;
            case WIN:
                if (!winGame(subject)) {
                    // Someone who can't win the level hit the winning block, so
                    // just treat collision normally:
                    subject.tick(collisionX, collisionY);
                }
                break;
            default:
                // no-op: presumably unset or unimplemented behavior upon collision
                LOGGER.log(java.util.logging.Level.FINE,
                        () -> String.format("Collision of type %s is not yet implemented.", targetBehavior.name()));
                break;
        }
    }

    /**
     * <p>
     * Here we define what happens when the enemy falls on a spike or bonks an enemy
     * on the head.
     * </p>
     * 
     * @param subject    From which subject's perspective we see the collision with.
     * @param collisionY In which direction we're colliding vertically.
     */
    private void handleDamageBlocks(MovingEntity subject, Direction collisionY) {
        if (collisionY == Direction.DOWN) {
            if (subject.getEntityType() == EntityType.ENEMY) {
                handleKillEnemy(subject);
            }
            if (subject.getEntityType() == EntityType.PLAYER) {
                killPlayer();
            }
        }
    }

    /**
     * <p>
     * What happens when we run into an enemy (either as player or enemy.
     * </p>
     * 
     * @param subject    From which subject's perspective we see the collision with.
     * @param target     The thing we're colliding with.
     * @param collisionX In which direction we're colliding horizontally.
     * @param collisionY In which direction we're colliding vertically.
     */
    private void handleCollisionWithEnemy(MovingEntity subject, Optional<Entity> target, Direction collisionX,
            Direction collisionY) {
        if (target.isEmpty()) {
            return;
        }

        // if collision on top -> we bonked / killed enemy
        if (collisionY == Direction.DOWN && collisionX == Direction.NONE) {
            handleKillEnemy(target.get());
            subject.jump();
        }
        if (subject.getEntityType() == EntityType.PLAYER) {
            if (collisionY == Direction.LEFT || collisionX == Direction.RIGHT) {
                // Player runs into enemy
                killPlayer();
            }
        } else if (subject.getEntityType() == EntityType.ENEMY) {
            Entity tgt = target.get();
            if (tgt.getEntityType() == EntityType.ENEMY) {
                // Enemy walks into enemy
                subject.setWalkDirection(
                        subject.getWalkDirection() == Direction.LEFT ? Direction.RIGHT : Direction.LEFT);
            }
            if (tgt.getEntityType() == EntityType.PLAYER) {
                // Enemy runs into player
                killPlayer();
            }
        }
    }

    /**
     * <p>
     * Kill enemy from the level. Stores it in the killed enemy array to be restored
     * later if the same level.
     * </p>
     * 
     * @param enemy The enemy to be freaking killed.
     */
    private void handleKillEnemy(Entity enemy) {
        Iterator<Enemy> iter = currentLevel.getEnemyList().values().iterator();
        while (iter.hasNext()) {
            Enemy e = iter.next();
            if (enemy == e) {
                killedEnemies.add(e);
                iter.remove();
                score.set(score.get() + 1); // we get a score if an enemy dies.
            }
        }
    }

    /**
     * <p>
     * Returns an array of entities around the Player P like so:
     * </p>
     * 
     * <pre>
     * <code>
     *      0_1_2
     *      9_P_3
     *      8_P_4
     *      7_6_5
     * </code>
     * </pre>
     * <p>
     * They don't necessarily collide! Needs to be checked later on.
     * </p>
     */
    private Entity[] getBlockAtPosOfPlr(Map<Position, Entity> map, Entity subject) {
        double x = subject.getPosition().getX();
        double y = subject.getPosition().getY();

        Entity[] blks = new Entity[MOVING_ENTITY_SURROUNDING_BLOCK_COUNT];
        Position[] blksPos = new Position[MOVING_ENTITY_SURROUNDING_BLOCK_COUNT];
        Position topL = new Position(x - (x % BLOCK_SIZE), y - (y % BLOCK_SIZE));
        Position topR = new Position((x + BLOCK_SIZE) - (x % BLOCK_SIZE), topL.getY());
        Position midL = new Position(topL.getX(), topL.getY() + BLOCK_SIZE);
        Position midR = new Position(topR.getX(), topR.getY() + BLOCK_SIZE);
        Position btmL = new Position(topL.getX(), topL.getY() + BLOCK_SIZE * 2);
        Position btmR = new Position(topR.getX(), topR.getY() + BLOCK_SIZE * 2);

        // Block on top of players head: 0 1 2
        // Blocks on the right of the player: 3 4
        // Blocks that the player's standing on: 5 6 7
        // Blocks on the left of the player: 8 9

        blksPos[0] = new Position(topL.getX() - BLOCK_SIZE, topL.getY() - BLOCK_SIZE);
        blksPos[1] = new Position(topL.getX(), topL.getY() - BLOCK_SIZE);
        blksPos[2] = new Position(topL.getX() + BLOCK_SIZE, topL.getY() - BLOCK_SIZE);
        blksPos[3] = new Position(topR.getX(), topR.getY());
        blksPos[4] = new Position(midR.getX(), midR.getY());
        blksPos[5] = new Position(btmR.getX(), btmR.getY());
        blksPos[6] = new Position(btmL.getX(), btmL.getY());
        blksPos[7] = new Position(btmL.getX() - BLOCK_SIZE, btmL.getY());
        blksPos[8] = new Position(midL.getX() - BLOCK_SIZE, midL.getY());
        blksPos[9] = new Position(topL.getX() - BLOCK_SIZE, topL.getY());

        for (int i = 0; i < MOVING_ENTITY_SURROUNDING_BLOCK_COUNT; i++) {
            blks[i] = map.get(blksPos[i]);
        }

        return blks;
    }

    /**
     * @param pack Path to a game pack
     */
    public void setGamePack(String pack) {
        packPath = pack;
    }

    /**
     * @param level
     */
    public void setLevel(String level) {
        levelName = level;
    }

    /**
     * @param currentLevel
     */
    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
    }

    /**
     * Resets the game state with the same level over again
     * 
     * @throws IOException
     * @throws InvalidLevelConfiguration
     */
    public void restartLevel() throws IOException, InvalidLevelConfiguration {
        stop();
        clearKilledEnemies();
        resetEnemiesAndPlayerToSpawnPosition();
        run(false);
    }

    /**
     * Resets all enemies to their original spawn position.
     */
    private void resetEnemiesAndPlayerToSpawnPosition() {
        for (Enemy enemy : currentLevel.getEnemyList().values()) {
            enemy.setPosition(enemy.getSpawnPosition());
        }
        plr.setPosition(plr.getSpawnPosition());
    }

    /**
     * <p>
     * We set the camera in the middle where the player is positioned at
     * </p>
     */
    private void resetCamToPlayer() {
        canvasController.setCamOffset(plr.getPosition().getX(), DEFAULT_Y_OFFSET);
    }

    /**
     * <p>
     * Stop the loop, restore entities and restore the killedEnemies list
     * </p>
     */
    public void stop() {
        loop.stop();
        currentLevel.restoreEnemies(killedEnemies);
        killedEnemies.clear();
    }

    /**
     * 
     * @param keyEventHandler
     */
    public void setKeyEventHandler(KeyEventHandler keyEventHandler) {
        this.keyEventHandler = keyEventHandler;
    }

    /**
     * @return player score
     */
    public IntegerProperty getScore() {
        return score;
    }

    /**
     * @return amount of lives the player still has
     */
    public IntegerProperty getLives() {
        return lives;
    }

    /**
     * @return whether the level was won
     */
    public BooleanProperty getLevelWasWonProperty() {
        return levelWasWon;
    }

    /**
     * <p>
     * Handle what happens when enemies or the player falls outside of the canvas
     * (falls off).
     * </p>
     */
    private void handleFallenEntities() {
        // First for Enemies
        Iterator<Enemy> iter = currentLevel.getEnemyList().values().iterator();
        while (iter.hasNext()) {
            Enemy e = iter.next();
            if (e.getPosition().getY() > canvasController.getCanvas().getHeight()) {
                killedEnemies.add(e);
                iter.remove();
            }
        }

        // Then for the player
        if (plr.getPosition().getY() > canvasController.getCanvas().getHeight()) {
            killPlayer();
        }
    }

    /**
     * <p>
     * Subtracts a life from the player and makes the player dead (can't play
     * anymore).
     * </p>
     */
    private void killPlayer() {
        lives.set(lives.get() - 1);
        try {
            restartLevel();
        } catch (IOException | InvalidLevelConfiguration e) {
            MessageHandler.createErrorWindow(e.getMessage());
        }
    }

    /**
     * <p>
     * Stops gameloop and messages GameController.
     * </p>
     * 
     * @return Was the level won by a valid entity (player, and not enemy?
     */
    private boolean winGame(MovingEntity movingEntity) {
        // Logically, only a player can win the game, otherwise enemies could run into
        // the flag and win the game
        if (movingEntity.getEntityType() == EntityType.PLAYER) {
            levelWasWon.set(true);
            stop();
            return true;
        }
        return false;
    }

    /**
     * <p>
     * Prepare the camera offset.
     * </p>
     */
    private void prepCamOffset() {
        double x = plr.getPosition().getX() - scrollLeftBorder;
        canvasController.setCamOffset(-x, 0);
    }

}
