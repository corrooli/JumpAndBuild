package ch.zhaw.pm3.teamretro.entity;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.JsonParser;
import ch.zhaw.pm3.teamretro.gamepack.entity.Entity;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityFactory;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;
import ch.zhaw.pm3.teamretro.gamepack.entity.Position;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Behavior;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;

public class EntityFactoryTest {

    static final String EXAMPLE_ENTITIES = "{\"entityList\":[{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":0,\"y\":384}},{\"entityType\":\"BLOCK\",\"sprite\":\"castle.block1\",\"properties\":{\"entityType\":\"BLOCK\",\"behavior\":\"STATIC\",\"solid\":true},\"position\":{\"x\":32,\"y\":384}}]}";

    static final String EXAMPLE_BASE = "entityList";

    static final JSONArray arr = JsonParser.stringToJSONObject(EXAMPLE_ENTITIES).getJSONArray(EXAMPLE_BASE);

    @Test
    public void testEntityFromJSON() throws InvalidLevelConfiguration {
        for (Object obj : arr) {
            JSONObject jobj = (JSONObject) obj;
            Entity e = EntityFactory.createEntity(jobj);
            JSONAssert.assertEquals(JsonParser.objToJson(e), jobj, false);
        }
    }

    @Test
    public void testEntityFromOthers() {
        JSONObject jobj = (JSONObject) arr.get(0);

        Sprite sprite = new Sprite("castle.block1");
        Properties prop = new Properties(Behavior.STATIC, EntityType.BLOCK, true);
        Position position = new Position(0, 384);

        Entity entity = EntityFactory.createEntity(prop.getEntityType(), sprite, position);

        JSONAssert.assertEquals(JsonParser.objToJson(entity), jobj, false);

    }
}
