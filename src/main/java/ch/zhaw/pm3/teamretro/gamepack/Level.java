package ch.zhaw.pm3.teamretro.gamepack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPropertyIgnore;
import org.json.JSONPropertyName;

import ch.zhaw.pm3.teamretro.gamepack.entity.Block;
import ch.zhaw.pm3.teamretro.gamepack.entity.Enemy;
import ch.zhaw.pm3.teamretro.gamepack.entity.Entity;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityFactory;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;
import ch.zhaw.pm3.teamretro.gamepack.entity.Item;
import ch.zhaw.pm3.teamretro.gamepack.entity.Player;
import ch.zhaw.pm3.teamretro.gamepack.entity.Position;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;
import javafx.util.Pair;

/**
 * The internal representation of the level configuration.
 */
public class Level {

    /**
     * The json name used to represent the level name
     */
    private static final String LEVEL_NAME_JSON = "name";

    /**
     * The json name used to represent the sprite packs
     */
    private static final String SPRITE_PACKS_JSON = "spritePacks";

    /**
     * The json name used to represent the background name.
     */
    private static final String BACKGROUND_JSON = "background";

    /**
     * The json name used to represent the entity lists.
     */
    private static final String ENTITY_LIST_JSON = "entityList";
    /**
     * The default for the background, used during the new level creation.
     */
    private static final String BACKGROUND_DEFAULT = "day.dayBackground";

    /**
     * The default for the sprite packs, used during the new level creation.
     */
    private static final Set<String> SPRITE_PACK_DEFAULT = new HashSet<>(Arrays.asList("protagonist", "day"));

    /**
     * The default for the entities, used during the new level creation.
     */
    private static final List<Entity> ENTITY_DEFAULT = Collections.singletonList(new Player());

    /**
     * The level name set.
     */
    private final String levelName;

    /**
     * The set of sprites packs this level needs to be initialized.
     */
    private final Set<String> spritePacks;

    /**
     * The map of all the entities including the player, as defined during
     * initialization.
     */
    private Map<Position, Entity> entityMap;

    /**
     * The background sprite used.
     */
    private Sprite background;

    /**
     * The map of all the currently spawned blocks.
     */
    private Map<Position, Block> blockMap;

    /**
     * The map of all the currently spawned items.
     */
    private Map<Position, Enemy> enemyMap;

    /**
     * The map of all the currently spawned items.
     */
    private Map<Position, Item> itemMap;

    /**
     * The player of the game.
     */
    private Player player;

    /**
     * Will construct the new level
     * 
     * @param levelName the new level name
     * @throws InvalidLevelConfiguration if something went wrong.
     */
    public Level(String levelName) throws InvalidLevelConfiguration {
        this(levelName, SPRITE_PACK_DEFAULT, BACKGROUND_DEFAULT, ENTITY_DEFAULT);
    }

    /**
     * Will construct the level.
     * 
     * @param levelName   the name of the level
     * @param spritePacks all the sprite packs needed for the initialization
     * @param background  the background
     * @param entities    all the entities that will live on this level
     * @throws InvalidLevelConfiguration if something went wrong
     */
    private Level(String levelName, Set<String> spritePacks, String background, List<Entity> entities)
            throws InvalidLevelConfiguration {
        this.levelName = levelName;
        this.spritePacks = new HashSet<>(spritePacks);
        this.background = new Sprite(background);
        this.entityMap = new ConcurrentHashMap<>();
        // prepare the entity hash map
        setupEntityMap(entities);
        setUpEntityLists();
    }

    public String toJson() {
        return JsonParser.objToJson(this);
    }

    @JSONPropertyName(LEVEL_NAME_JSON)
    public String getLevelName() {
        return levelName;
    }

    public void addSpritePack(String name) {
        // can be directly added, due to the attributes of a HashSet
        spritePacks.add(name);
    }

    public Set<String> getSpritePacks() {
        return Collections.unmodifiableSet(spritePacks);
    }

    public List<Entity> getEntityList() {
        return List.copyOf(entityMap.values());
    }

    public void setEntityList(List<Entity> entityList) {
        setupEntityMap(entityList);
    }

    /**
     * This method will convert from a list to the internally used hash map.
     *
     * @param entityList the entities
     */
    private void setupEntityMap(List<Entity> entityList) {
        this.entityMap = entityList.stream().collect(Collectors.toMap(Entity::getPosition, entity -> entity));
    }

    /**
     * Will returned killed enemies to the map.
     *
     * @param killedEnemies a list of enemies
     */
    public void restoreEnemies(List<Enemy> killedEnemies) {
        for (Enemy enemies : killedEnemies) {
            enemyMap.put(enemies.getPosition(), enemies);
        }
    }

    @JSONPropertyName(BACKGROUND_JSON)
    public String getBackgroundName() {
        return background.getName();
    }

    @JSONPropertyIgnore
    public Sprite getBackground() {
        return background;
    }

    public void setBackground(Sprite background) {
        this.background = background;
    }

    /**
     * This will get a modifiable copy of the internal block lists.
     * <p>
     * Attention modifying this list will not really result in any changes inside of
     * the level. So any permanent modifications, have to be done via
     * {@link #removeEntity(Position) removeEntity} or
     * {@link #addEntity(EntityType, Sprite, Position) addEntity}.
     *
     * @return a modifiable copy of the block list
     */
    @JSONPropertyIgnore
    public Map<Position, Block> getBlockList() {
        return blockMap;
    }

    /**
     * This will get a modifiable copy of the internal enemy lists.
     * <p>
     * Attention modifying this list will not really result in any changes inside of
     * the level. So any permanent modifications, have to be done via
     * {@link #removeEntity(Position) removeEntity} or
     * {@link #addEntity(EntityType, Sprite, Position) addEntity}.
     *
     * @return a modifiable copy of the enemy list
     */
    @JSONPropertyIgnore
    public Map<Position, Enemy> getEnemyList() {
        return enemyMap;
    }

    /**
     * This will get a modifiable copy of the internal item lists.
     * <p>
     * Attention modifying this list will not really result in any changes inside of
     * the level. So any permanent modifications, have to be done via
     * {@link #removeEntity(Position) removeEntity} or
     * {@link #addEntity(EntityType, Sprite, Position) addEntity}.
     *
     * @return a modifiable copy of the item list
     */
    @JSONPropertyIgnore
    public Map<Position, Item> getItemList() {
        return itemMap;
    }

    @JSONPropertyIgnore
    public Player getPlayer() {
        return player;
    }

    /**
     * Will split up the entities into there corresponding location.
     *
     * @throws InvalidLevelConfiguration If an invalid level configuration was
     *                                   encountered.
     */
    private void setUpEntityLists() throws InvalidLevelConfiguration {
        blockMap = new ConcurrentHashMap<>(blockMap != null ? blockMap.size() : 0);
        enemyMap = new ConcurrentHashMap<>(enemyMap != null ? blockMap.size() : 0);
        itemMap = new ConcurrentHashMap<>(itemMap != null ? blockMap.size() : 0);
        player = null;
        for (Entity entity : entityMap.values()) {
            switch (entity.getEntityType()) {
                case BLOCK:
                    blockMap.put(entity.getPosition(), (Block) entity);
                    break;
                case ITEM:
                    itemMap.put(entity.getPosition(), (Item) entity);
                    break;
                case PLAYER:
                    if (player != null) {
                        throw new InvalidLevelConfiguration("There may not be more the a single player per level.");
                    }
                    player = (Player) entity;
                    break;
                case ENEMY:
                    enemyMap.put(entity.getPosition(), (Enemy) entity);
                    break;
                default:
                    throw new EnumConstantNotPresentException(EntityType.class, entity.getEntityType().name());
            }
        }
    }

    /**
     * Will add a new entity to the blocks.
     * <p>
     * This method returns a pair with the first element (the key) being the newly
     * created entity, the second (the value) will be the possible old or removed
     * entity from the given position.
     *
     * @param type     the type of entity to be created
     * @param sprite   the sprite to base the new block on
     * @param position the new position to place the new entity on
     * @return returns the removed entity from the given block if there was one
     * @throws InvalidLevelConfiguration If an invalid level configuration was
     *                                   encountered.
     */
    public Pair<Optional<Entity>, Optional<Entity>> addEntity(EntityType type, Sprite sprite, Position position)
            throws InvalidLevelConfiguration {
        // call remove entity with the rebuild flag false as not to rebuild the whole
        // lists
        Optional<Entity> oldEntities = removeEntity(position, false);

        if (type == EntityType.PLAYER) {
            // don't rebuild list here, as it will be rebuild later on
            oldEntities = removeEntity(player.getSpawnPosition(), false);
        }

        Entity newEntity = EntityFactory.createEntity(type, sprite, position);
        entityMap.put(position, newEntity);

        // just add the sprite pack to the level, this is okay as we are talking about a
        // HashSet
        addSpritePack(sprite.getSpriteInformation().getPackName());

        // rebuild the data arrays
        setUpEntityLists();
        return new Pair<>(Optional.of(newEntity), oldEntities);
    }

    /**
     * Will remove an old entity from the database and assuming there was an entity
     * to remove, it will be returned inside of the optional.
     *
     * @param position the position to check on
     * @return will return an optional with an entity inside
     * @throws InvalidLevelConfiguration If an invalid level configuration was
     *                                   encountered.
     */
    public Optional<Entity> removeEntity(Position position) throws InvalidLevelConfiguration {
        Entity entity = entityMap.get(position);
        // if the entity was the player, raise hell
        if (entity != null && entity.getEntityType() == EntityType.PLAYER) {
            throw new InvalidLevelConfiguration(String.format(
                    "Tried to remove an entity as position <%s>.%nThere always must be a player available.",
                    entity.getPosition()));
        }
        return removeEntity(position, true);
    }

    /**
     * This method removes an entity from the given position without regard to
     * entity typed ('brutal' removal). It will return that entity, if it exists
     * inside of an optional.
     *
     * @param position     the position to check on
     * @param rebuildLists in case the entity setup list has to be rebuilt.
     * @return will return an optional with an entity inside
     * @throws InvalidLevelConfiguration If an invalid level configuration was
     *                                   encountered.
     */
    private Optional<Entity> removeEntity(Position position, boolean rebuildLists) throws InvalidLevelConfiguration {
        Optional<Entity> removedEntity = Optional.empty();

        Entity entity = entityMap.remove(position);

        if (entity != null) {
            removedEntity = Optional.of(entity);
        }

        if (rebuildLists) {
            setUpEntityLists();
        }

        return removedEntity;
    }

    /**
     * Will generate a new level from a json string.
     *
     * @param json the json string
     * @return a initialized level class
     * @throws InvalidLevelConfiguration If an invalid level configuration was
     *                                   encountered.
     */
    public static Level valueOf(String json) throws InvalidLevelConfiguration {
        JSONObject jsonObject = JsonParser.stringToJSONObject(json);

        String levelName = jsonObject.getString(LEVEL_NAME_JSON);

        Set<String> spritePacks = jsonObject.getJSONArray(SPRITE_PACKS_JSON).toList().stream().map(sp -> (String) sp)
                .collect(Collectors.toSet());

        String background = jsonObject.getString(BACKGROUND_JSON);
        JSONArray entitiesRaw = jsonObject.getJSONArray(ENTITY_LIST_JSON);

        List<Entity> entityList = new ArrayList<>(entitiesRaw.length());

        for (Object e : entitiesRaw) {
            JSONObject entity = (JSONObject) e;
            entityList.add(EntityFactory.createEntity(entity));
        }

        return new Level(levelName, spritePacks, background, entityList);
    }
}
