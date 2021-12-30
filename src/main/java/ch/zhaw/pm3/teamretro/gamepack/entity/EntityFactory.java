package ch.zhaw.pm3.teamretro.gamepack.entity;

import org.json.JSONObject;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.JsonParser;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;

/**
 * A factory with the job of initializing the different entities.
 */
public class EntityFactory {

    /**
     * Private constructor because it's a utility class and instantiation should be
     * prohibited.
     */
    private EntityFactory() {
    }

    /**
     * Will create a new entity type from a given json object.
     * 
     * @param entity the json object to create the Entity of
     * @return an entity object
     * @throws InvalidLevelConfiguration if the given json data in incorrect
     */
    public static Entity createEntity(JSONObject entity) throws InvalidLevelConfiguration {
        EntityType eType = EntityType.valueOf(entity.getString("entityType").toUpperCase());
        switch (eType) {
            case BLOCK:
                return new Block(entity);
            case ITEM:
                return new Item(entity);
            case PLAYER:
                return new Player(entity);
            case ENEMY:
                return new Enemy(entity);
            default:
                throw new EnumConstantNotPresentException(EntityType.class, eType.name());
        }
    }

    /**
     * Will create a new entity type from a given json string.
     * 
     * @param json the json object to create the Entity of
     * @return an entity object
     * @throws InvalidLevelConfiguration if the given json data in incorrect
     */
    public static Entity createEntity(String json) throws InvalidLevelConfiguration {
        return createEntity(JsonParser.stringToJSONObject(json));
    }

    /**
     * Will create a new entity object from the given constructs.
     * 
     * @param type     the type
     * @param sprite   the sprite
     * @param position the position
     * @return an entity object
     */
    public static Entity createEntity(EntityType type, Sprite sprite, Position position) {
        switch (type) {
            case BLOCK:
                return new Block(position, sprite, sprite.getProperties());
            case ITEM:
                return new Item(position, sprite, sprite.getProperties());
            case PLAYER:
                return new Player(position, sprite, sprite.getProperties());
            case ENEMY:
                return new Enemy(position, sprite, sprite.getProperties());
            default:
                throw new EnumConstantNotPresentException(EntityType.class, type.name());
        }
    }
}
