package ch.zhaw.pm3.teamretro.gamepack.entity;

import org.json.JSONObject;
import org.json.JSONPropertyIgnore;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;
import javafx.scene.shape.Rectangle;

/**
 * Represents an abstract moving entity. This class creates the basis needed to
 * build the {@link Player} and the {@link Enemy}.
 */
public abstract class MovingEntity extends Entity {

    /**
     * After how many milliseconds [ms] do we cycle the walking animation?
     */
    private static final double WALK_ANIMATION_TIME = 100;

    /**
     * Predetermined jump velocity.
     */
    private static final double JUMP_VELOCITY = -40;

    /**
     * upper speed limit per tick is half a block.
     */
    private static final double VELOCITY_CAP = 32.0 / 4.0;

    /**
     * The default speed to move at.
     */
    private static final int STANDARD_SPEED = 14;

    /**
     * The current speed of the entity.
     */
    private int speed = STANDARD_SPEED;

    /**
     * The current velocity of the entity in relation to it's current location.
     */
    private Position velocity = new Position(0, 0);

    /**
     * How strongly gravity is currently pulling the entity down.
     * <p>
     * Velocity downwards.
     */
    private double gravity = 0.0;

    /**
     * Same as gravity = 0;
     */
    private boolean onGround = false;

    /**
     * Current walking direction.
     */
    private Direction walkDirection = Direction.LEFT;

    /**
     * The time used to do a movement.
     */
    private double walkTimeActual = WALK_ANIMATION_TIME;

    /**
     * Time passed since last walk cycle. Unit: [ms]
     */
    private double timeSinceLastWalkCycle = 0;

    /**
     * Needed for when true, don't apply gravity
     */
    private boolean isColliding = false;

    /**
     * As this is an abstract class this constructor here, will not do much.
     *
     * @param jsonObject JSON object data.
     * @throws InvalidLevelConfiguration In case the creation leads into an illegal
     *                                   level configuration.
     */
    protected MovingEntity(JSONObject jsonObject) throws InvalidLevelConfiguration {
        super(jsonObject);
    }

    /**
     * Constructor to be used when creating an entity.
     *
     * @param entityType Type of entity.
     * @param position   Initial position of entity.
     * @param sprite     Sprite (image) of entity.
     * @param properties Properties of entity.
     */
    protected MovingEntity(EntityType type, Position position, Sprite sprite, Properties properties) {
        super(type, position, sprite, properties);
    }

    /**
     * Moves the entity by a given deviation.
     *
     * @param delta New position.
     */
    public void move(Position delta) {
        setPosition(position.getX() + delta.getX(), position.getY() + delta.getY());
    }

    /**
     * Will add an offset to the current velocity.
     *
     * @param x the offset for x
     * @param y the offset for y
     */
    public void addToVelocity(double x, double y) {
        setVelocity(velocity.getX() + x, velocity.getY() + y);
    }

    /**
     * Determines collision and velocity handling for each given entity, will be
     * called after each frame.
     *
     * @param collisionX -1 means collision left, +1 means right
     * @param collisionY -1 means collision top, +1 means bottom
     */
    @JSONPropertyIgnore
    public void tick(Direction collisionX, Direction collisionY) {
        addToVelocity(0, gravity);

        double newPositionX = position.getX() + velocity.getX();
        double newPositionY = position.getY() + velocity.getY();

        switch (collisionX) {
            case LEFT: // left
                newPositionX = clampPositionToGrid(newPositionX);
                velocity.setX(0);
                walkDirection = Direction.RIGHT;
                break;
            case RIGHT: // right
                newPositionX = clampPositionToGrid(newPositionX);
                velocity.setX(0);
                walkDirection = Direction.LEFT;
                break;
            default: // default to keep lint happy
                break;
        }

        switch (collisionY) {
            case UP: // up / ceiling case
                newPositionY = clampPositionToGrid(newPositionY);
                velocity.setY(1);
                onGround = false;
                break;
            case DOWN: // down / floor case
                newPositionY = clampPositionToGrid(newPositionY);
                velocity.setY(0);
                // Player now's standing on solid ground (!!!)
                onGround = true;
                break;
            default: // default to keep lint happy
                break;
        }

        setPosition(newPositionX, newPositionY);
    }

    /**
     * Helper method, clamps a given coordinate to the nearest multiple of 32
     * (corresponding to block grid)
     *
     * @param position Position to be clamped.
     * @return Clamped position.
     */
    private static double clampPositionToGrid(double position) {
        double clampedPosition = position % 32;
        position = position + (clampedPosition < 32 / 2.0 ? -clampedPosition : -clampedPosition + 32);
        return position;
    }

    /**
     * Moves an entity for test purposes.
     */
    @JSONPropertyIgnore
    public void tick() {
        move(new Position(velocity.getX(), velocity.getY()));
    }

    /**
     * Switches through walking animations, will be called after each frame.
     *
     * @param lastFrameTime Last frame time.
     * @return True when the walk animation should be cycled, false if not.
     */
    @JSONPropertyIgnore
    public boolean tickWalkAnimations(double lastFrameTime) {
        timeSinceLastWalkCycle += lastFrameTime * 1E3;
        if (timeSinceLastWalkCycle >= walkTimeActual) {
            timeSinceLastWalkCycle = 0;
            return true; // walk animation should happen !
        }
        return false;
    }

    /**
     * Determines the behavior of an entity when jumping.
     */
    @JSONPropertyIgnore
    public void jump() {
        setVelocity(0, JUMP_VELOCITY * gravity);
    }

    /**
     * @return Bounding box after gravity and movement (velocity vector) has been
     *         applied to the entity.
     * @throws InvalidLevelConfiguration In case an illegal level configuration was
     *                                   encountered.
     */
    @JSONPropertyIgnore
    public Rectangle getBoundingBoxAfterTick() throws InvalidLevelConfiguration {
        double newX = position.getX() + getVelocity().getX();
        double newY = position.getY();
        double deltaY = getVelocity().getY() + gravity;

        if (deltaY > VELOCITY_CAP) {
            deltaY = VELOCITY_CAP;
        } else if (deltaY < -VELOCITY_CAP) {
            deltaY = -VELOCITY_CAP;
        }

        newY += deltaY;

        return new Rectangle(newX, newY, getCurrentImage().getWidth(), getCurrentImage().getHeight());
    }

    /**
     * Determines the walk style for the protagonist.
     *
     * @param fast If the protagonist is going fast.
     */
    public void setWalkStyle(boolean fast) {
        walkTimeActual = WALK_ANIMATION_TIME;
        // walk animation time represents the interval
        // in which the movement circles, so shorter
        // interval means faster movement speed.
        if (fast) {
            walkTimeActual *= 0.5;
        }
    }

    /**
     * Sets an entities' gravity constant. Needs to be updated to adjust for
     * inconsistent frame rate.
     *
     * @param gravity Gravity constant.
     */
    public void setGravity(double gravity) {
        this.gravity = gravity;
    }

    @JSONPropertyIgnore
    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public void setWalkDirection(Direction walkDirection) {
        this.walkDirection = walkDirection;
    }

    @JSONPropertyIgnore
    public Direction getWalkDirection() {
        return walkDirection;
    }

    @JSONPropertyIgnore
    public boolean isColliding() {
        return isColliding;
    }

    public void setColliding(boolean isColliding) {
        this.isColliding = isColliding;
    }

    @JSONPropertyIgnore
    public Position getVelocity() {
        return velocity;
    }

    /**
     * Will set the current velocity of the entity per tick. Attention this method
     * will cap the max speed per axis to {@value #VELOCITY_CAP}.
     * 
     * @param x the speed along the x-axis
     * @param y the speed along the y-axis
     */
    public void setVelocity(double x, double y) {
        double dx = x;
        double dy = y;
        if (x > VELOCITY_CAP) {
            dx = VELOCITY_CAP;
        } else if (x < -VELOCITY_CAP) {
            dx = -VELOCITY_CAP;
        }
        if (y > VELOCITY_CAP) {
            dy = VELOCITY_CAP;
        } else if (y < -VELOCITY_CAP) {
            dy = -VELOCITY_CAP;
        }
        this.velocity = new Position(dx, dy);
    }

    @JSONPropertyIgnore
    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int amount) {
        speed = amount;
    }

    public void changeSpeed(int amount) {
        speed += amount;
    }
}
