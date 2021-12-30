package ch.zhaw.pm3.teamretro.gamepack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import ch.zhaw.pm3.teamretro.gamepack.entity.Block;
import ch.zhaw.pm3.teamretro.gamepack.entity.Entity;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;
import ch.zhaw.pm3.teamretro.gamepack.entity.Player;
import ch.zhaw.pm3.teamretro.gamepack.entity.Position;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Behavior;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;
import javafx.util.Pair;

class LevelTest {

    static final String START_LEVEL_JSON = "{\"entityList\":[{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":0,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":32,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":64,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":96,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":128,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":160,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.spikes\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":160,\"y\":352}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":192,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":224,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block2\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":256,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.dirtBottom\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":288,\"y\":384}},{\"entityType\":\"ENEMY\",\"sprite\":\"enemies.sonichu\",\"properties\":{\"entityType\":\"ENEMY\",\"behavior\":\"GENERICENEMY\",\"solid\":true},\"position\":{\"x\":128,\"y\":576}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"WIN\",\"solid\":true},\"position\":{\"x\":128,\"y\":608}},{\"entityType\":\"BLOCK\",\"sprite\":\"levelelements.goalPost\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"WIN\",\"solid\":true},\"position\":{\"x\":2,\"y\":578}},{\"entityType\":\"PLAYER\",\"sprite\":\"protagonist.protagonist\",\"properties\":{\"entityType\":\"PLAYER\",\"behavior\":\"PLAYABLE\",\"solid\":true},\"position\":{\"x\":128,\"y\":128}},{\"entityType\":\"BLOCK\",\"sprite\":\"day.cloudL\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":false},\"position\":{\"x\":0,\"y\":0}},{\"entityType\":\"BLOCK\",\"sprite\":\"day.cloudR\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":false},\"position\":{\"x\":32,\"y\":0}}],\"background\":\"castle.castleBackground\",\"spritePacks\":[\"day\",\"levelelements\",\"protagonist\",\"castle\",\"enemies\"],\"name\":\"startLevel\"}";
    static final String LEVEL_NAME = "Test Level";
    static final String PLAYER_SPRITE = "protagonist.protagonist";
    static final Position PROTATONIST_POSITION = new Position(128, 128);

    Level level;
    Level startLevel;

    @BeforeEach
    void setup() throws InvalidLevelConfiguration {
        setUpGeneral();
        setupStartLevel();
    }

    void setupStartLevel() throws InvalidLevelConfiguration {
        startLevel = Level.valueOf(START_LEVEL_JSON);
    }

    void setUpGeneral() throws InvalidLevelConfiguration {
        Sprite dirtBlock = new Sprite("retro.dirt");
        level = new Level(LEVEL_NAME);
        level.addSpritePack("retro");
        level.setBackground(new Sprite("retro.background"));
        List<Entity> el = new ArrayList<>();
        Properties prop = new Properties(Behavior.STATIC, EntityType.ENEMY, true);
        el.add(new Block(new Position(1, 0), dirtBlock, prop));
        el.add(new Block(new Position(1, 1), dirtBlock, prop));
        el.add(new Block(new Position(1, 2), dirtBlock, prop));
        el.add(new Player(new Position(2, 1), new Sprite("enemies.sonichu"), prop));
        level.setEntityList(el);
    }

    @Test
    void testRemoveEntity() {
        Optional<Entity> entity = Optional.empty();
        {
            // random block
            Position toRemove = new Position(32, 0);
            EntityType eType = EntityType.BLOCK;
            String spriteName = "day.cloudR";
            Properties propE = new Properties(Behavior.STATIC, eType, false);
            try {
                entity = startLevel.removeEntity(toRemove);
            } catch (InvalidLevelConfiguration e) {
                e.printStackTrace(); // should be unreachable :)
            }
            assertTrue(entity.isPresent(), "Checking if the entity is removed.");
            Entity entity2 = entity.get();
            assertEquals(spriteName, entity2.getSprite().getName(), "Checking if the sprite name maches.");
            Properties propA = entity2.getProperties();
            assertEquals(propE, propA, "Comparing properties.");
        }
        {
            // player
            Position toRemove = new Position(128, 128);
            InvalidLevelConfiguration ilc = assertThrows(InvalidLevelConfiguration.class,
                    () -> startLevel.removeEntity(toRemove));
            String expected = String.format(
                    "Tried to remove an entity as position <%s>.%nThere always must be a player available.", toRemove);
            assertEquals(expected, ilc.getMessage());
        }
    }

    @Test
    void testRemovePlayer() {
        InvalidLevelConfiguration ilc = assertThrows(InvalidLevelConfiguration.class, () -> {
            startLevel.removeEntity(PROTATONIST_POSITION);
        });
        assertEquals(
                String.format("Tried to remove an entity as position <%s>.%nThere always must be a player available.",
                        PROTATONIST_POSITION),
                ilc.getMessage());

    }

    @Test
    void testMovePlayer() throws InvalidLevelConfiguration {
        Position newPosition = new Position(42, 42);
        Pair<Optional<Entity>, Optional<Entity>> oldPlayer = startLevel.addEntity(EntityType.PLAYER,
                new Sprite(PLAYER_SPRITE), newPosition);

        // check the players old and new position
        assertTrue(oldPlayer.getKey().isPresent());
        assertTrue(oldPlayer.getValue().isPresent());
    }

    @Test
    void testAddEntity() throws InvalidLevelConfiguration {
        // a block
        Position pos = new Position(32, 0);
        String spriteName = "some.cloudR";
        Pair<Optional<Entity>, Optional<Entity>> entity = startLevel.addEntity(EntityType.ENEMY, new Sprite(spriteName),
                pos);

        // make sure that you get the removed values and the new ones as well
        assertTrue(entity.getKey().isPresent());
        assertTrue(entity.getValue().isPresent());
        { // new block
            EntityType eType = EntityType.BLOCK;
            Properties prop = new Properties(Behavior.STATIC, eType, true);
            assertEquals(spriteName, entity.getKey().get().getSprite().getName(),
                    "Checking if the sprite name matches.");
            assertEquals(prop, entity.getKey().get().getProperties(), "Comparing properties.");
        }
        { // old enemy
            EntityType eType = EntityType.BLOCK;
            Properties prop = new Properties(Behavior.STATIC, eType, true);
            assertEquals(spriteName, entity.getKey().get().getSprite().getName(),
                    "Checking if the sprite name matches.");
            assertEquals(prop, entity.getKey().get().getProperties(), "Comparing properties.");
        }
    }

    @Test
    void toJson() {
        JSONAssert.assertEquals(START_LEVEL_JSON, startLevel.toJson(), false);
    }

    @Test
    void toObj() throws InvalidLevelConfiguration {
        String json = level.toJson();
        Level levelFromJson;
        levelFromJson = Level.valueOf(json);
        assertEquals(LEVEL_NAME, levelFromJson.getLevelName());
        assertEquals(level.getEntityList().size(), levelFromJson.getEntityList().size());
    }
}