package ch.zhaw.pm3.teamretro.gamepack.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONArray;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.JsonParser;

class MovingEntityTest {

    static final String ENTITIES = "{\"entityList\":[{\"entityType\":\"ENEMY\",\"sprite\":\"enemies.sonichu\",\"properties\":{\"entityType\":\"ENEMY\",\"behavior\":\"GENERICENEMY\",\"solid\":true},\"position\":{\"x\":128,\"y\":576}},{\"entityType\":\"PLAYER\",\"sprite\":\"protagonist.protagonist\",\"properties\":{\"entityType\":\"PLAYER\",\"behavior\":\"PLAYABLE\",\"solid\":true},\"position\":{\"x\":128,\"y\":128}}]}";

    Player player;
    Enemy enemy;

    @BeforeEach
    void setup() throws JSONException, InvalidLevelConfiguration {
        JSONArray jObj = JsonParser.stringToJSONObject(ENTITIES).getJSONArray("entityList");
        player = (Player) EntityFactory.createEntity(jObj.getJSONObject(1));
        enemy = (Enemy) EntityFactory.createEntity(jObj.getJSONObject(0));
    }

    @Test
    void move() {
        Position pos = new Position(128, 576);
        assertEquals(pos, enemy.getPosition());
        enemy.move(new Position(40, -78.1));
        pos.setX(pos.getX() + 40);
        pos.setY(pos.getY() + -78.1);
        assertEquals(pos, enemy.getPosition());
        enemy.move(new Position(0, 0));
        assertEquals(pos, enemy.getPosition());
        assertThrows(NullPointerException.class, () -> enemy.move(null));
    }

    @Test
    void testVelocity() {
        assertEquals(new Position(0, 0), player.getVelocity());
        player.setVelocity(10, 0);
        assertEquals(new Position(8, 0), player.getVelocity());
        player.addToVelocity(-3, 2);
        assertEquals(new Position(5, 2), player.getVelocity());
    }

    @Test
    void testTick() {
        Position pos = new Position(128, 576);
        assertEquals(pos, enemy.getPosition());
        enemy.setVelocity(2, -78.1);
        assertEquals(new Position(2, -8), enemy.getVelocity());
        pos.setX(pos.getX() + 2);
        pos.setY(pos.getY() + -8);
        enemy.tick();
        assertEquals(pos, enemy.getPosition());
    }

    @Test
    void testColliding() {
        assertFalse(player.isColliding());
        player.setColliding(true);
        assertTrue(player.isColliding());
    }
}
