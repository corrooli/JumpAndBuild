package ch.zhaw.pm3.teamretro.gamepack.entity;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONPropertyIgnore;
import org.json.JSONPropertyName;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Animation;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

/**
 * Entity object. Every element in a level (player, enemy, block) is an entity
 * and feature similar abilities. Behavior will be defined via properties.
 */
public abstract class Entity {

    /**
     * The json name used to represents the {@link EntityType} of this entity.
     */
    private static final String ENTITY_TYPE_JSON = "entityType";

    /**
     * The json name used to represents the {@link Position} of this entity.
     */
    private static final String POSITION_JSON = "position";

    /**
     * The json name used to represents the name of the {@link Sprite} of this
     * entity.
     */
    private static final String SPRITE_JSON = "sprite";

    /**
     * The json name used to represents the {@link Properties} of this entity.
     */
    private static final String PROPERTIES_JSON = "properties";

    /**
     * The default animation index, from where to start.
     */
    private static final int ANIMATION_INDEX_DEFAULT = 0;

    /**
     * The type of entity this represents. This is set during initialization, will
     * never change and is used in a switch to initialize the correct type.
     */
    private final EntityType entityType;

    /**
     * The sprite from which the images originate.
     */
    private Sprite sprite;

    /**
     * The set properties of the entity, the original properties originate from the
     * sprite information.
     */
    private Properties properties;

    /**
     * The current position of the entity.
     */
    protected Position position;

    /**
     * The position in which the entity starts out in, defaults to the position.
     */
    private final Position spawnPosition;

    /**
     * Used to show the currently chosen animation style.
     */
    private Animation currentAnimation = Animation.IDLE;

    /**
     * The entities bounding box used to calculate the intersections, with the
     * surrounding.
     */
    private final Rectangle boundingBox; // bounding box

    /**
     * Used for index of the current animation index.
     */
    private int currentAnimationIndex = ANIMATION_INDEX_DEFAULT;

    /**
     * If the current sprite image shall be flipped on render.
     */
    private boolean flipped = false;

    /**
     * Constructor to be used when creating an entity.
     *
     * @param entityType Type of entity.
     * @param position   Initial position of entity.
     * @param sprite     Sprite (image) of entity.
     * @param properties Properties of entity.
     */
    protected Entity(EntityType entityType, Position position, Sprite sprite, Properties properties) {
        this.properties = properties;
        this.entityType = entityType;
        this.position = position;
        this.sprite = sprite;

        // Note: width/height must be set dynamically due to sprites potentially
        // changing
        this.boundingBox = new Rectangle(position.getX(), position.getY(), 0, 0);
        this.spawnPosition = new Position(position.getX(), position.getY()); // create spawn point
    }

    /**
     * Constructor for creating entities out of a JSON object.
     *
     * @param jsonObject JSON object data.
     * @throws InvalidLevelConfiguration In case the creation leads into an illegal
     *                                   level configuration.
     */
    protected Entity(JSONObject jsonObject) throws InvalidLevelConfiguration {
        try {
            this.entityType = EntityType.valueOf(jsonObject.getString(ENTITY_TYPE_JSON).toUpperCase());
            this.position = Position.valueOf(jsonObject.getJSONObject(POSITION_JSON));
            this.sprite = new Sprite(jsonObject.getString(SPRITE_JSON));
            this.properties = Properties.valueOf(jsonObject.getJSONObject(PROPERTIES_JSON));
            // Note: width/height must be set dynamically due to sprites potentially
            // changing
            this.boundingBox = new Rectangle(position.getX(), position.getY(), 0, 0);
            this.spawnPosition = new Position(position.getX(), position.getY()); // create spawn point
        } catch (JSONException e) {
            throw new InvalidLevelConfiguration(String.format("JSON configuration not valid <%s>", e.getMessage()));
        }
    }

    /**
     * Sets a new position for an entity. Updates the bounding box right after.
     *
     * @param pos New Position.
     */
    public void setPosition(Position pos) {
        this.position = pos;
        updateBoundingBox();
    }

    /**
     * Sets a new position for an entity. Updates the bounding box right after.
     *
     * @param x new x coordinate.
     * @param y new y coordinate.
     */
    public void setPosition(double x, double y) {
        setPosition(new Position(x, y));
    }

    /**
     * Sets a new position for the bounding box.
     */
    private void updateBoundingBox() {
        boundingBox.setX(position.getX());
        boundingBox.setY(position.getY());
    }

    /**
     * Will build up the bounding box required for collision analysis.
     * 
     * @return a the current bounds
     * @throws InvalidLevelConfiguration if there was something wrong
     */
    @JSONPropertyIgnore
    public Rectangle getBoundingBox() throws InvalidLevelConfiguration {
        this.boundingBox.setWidth(getCurrentImage().getWidth());
        this.boundingBox.setHeight(getCurrentImage().getHeight());
        return boundingBox;
    }

    /**
     * Toggles between a flipped an normal image, e.g. when the player or an enemy
     * changes direction.
     */
    public void toggleFlipped() {
        flipped = !flipped;
    }

    @JSONPropertyIgnore
    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean val) {
        flipped = val;
    }

    /**
     * Will return the currently set sprite image.
     *
     * @return an Image instance.
     * @throws InvalidLevelConfiguration In case an illegal level configuration was
     *                                   encountered.
     */
    @JSONPropertyIgnore
    public Image getCurrentImage() throws InvalidLevelConfiguration {
        List<Image> images = sprite.getImages(currentAnimation);

        if (images.isEmpty()) {
            throw new InvalidLevelConfiguration(
                    String.format("The sprite called <%s> does not contain the animation type <%s>.", sprite.getName(),
                            currentAnimation.name()));
        }

        return images.get(currentAnimationIndex);
    }

    /**
     * Will return the next available image from the animation list. Will reset the
     * counter back to the default if the animation changes. if return value isn't
     * needed
     *
     * @param animation the type of animation
     * @return a single image
     * @throws InvalidLevelConfiguration In case an illegal level configuration was
     *                                   encountered.
     */
    public void getNextImage(Animation animation) throws InvalidLevelConfiguration {
        List<Image> images = sprite.getImages(animation);

        if (images.isEmpty()) {
            throw new InvalidLevelConfiguration(
                    String.format("The sprite called <%s> does not contain the animation type <%s>.", sprite.getName(),
                            animation.name()));
        }

        if (currentAnimation != animation) {
            currentAnimation = animation;
            currentAnimationIndex = ANIMATION_INDEX_DEFAULT; // reset state
        }
        // adding to the next step then looping back via modulo if needed
        currentAnimationIndex = (currentAnimationIndex + 1) % images.size();
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @JSONPropertyIgnore
    public Position getPosition() {
        return position;
    }

    @JSONPropertyName(POSITION_JSON)
    public Position getSpawnPosition() {
        return spawnPosition;
    }

    @JSONPropertyName(SPRITE_JSON)
    public String getSpriteName() {
        return sprite.getSpriteInformation().getFullSpriteName();
    }

    @JSONPropertyIgnore
    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Properties getProperties() {
        return properties;
    }

}
