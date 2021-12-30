package ch.zhaw.pm3.teamretro.gamepack.sprite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.zhaw.pm3.teamretro.gamepack.JsonParser;

class SpritePackTest {

    static final String SPRITE_PACK_JSON = "{\"bushL\":{\"name\":\"Bush Left\",\"properties\":{\"entityType\":\"block\",\"behavior\":\"static\",\"solid\":false},\"sprites\":{\"idle\":[\"blocks/bush_l.png\"]}},\"bushR\":{\"name\":\"Bush Right\",\"properties\":{\"entityType\":\"block\",\"behavior\":\"static\",\"solid\":false},\"sprites\":{\"idle\":[\"blocks/bush_r.png\"]}},\"dirtLeft\":{\"name\":\"Dirt Left\",\"properties\":{\"entityType\":\"block\",\"behavior\":\"static\",\"solid\":true},\"sprites\":{\"idle\":[\"blocks/dirt_left.png\"]}},\"dirtRight\":{\"name\":\"Dirt Right\",\"properties\":{\"entityType\":\"block\",\"behavior\":\"static\",\"solid\":true},\"sprites\":{\"idle\":[\"blocks/dirt_right.png\"]}},\"dirtSigle\":{\"name\":\"Dirt Single\",\"properties\":{\"entityType\":\"block\",\"behavior\":\"static\",\"solid\":true},\"sprites\":{\"idle\":[\"blocks/dirt_single.png\"]}},\"dirtTop\":{\"name\":\"Dirt Top\",\"properties\":{\"entityType\":\"block\",\"behavior\":\"static\",\"solid\":true},\"sprites\":{\"idle\":[\"blocks/dirt_top.png\"]}}}";

    static final String SPRITE_PACK_NAME = "snow";

    static final String SPRITE_PACK_PATH = "assets";

    static SpritePack spritePack;

    @BeforeAll
    static void setup() {
        spritePack = SpritePack.valueOf(SPRITE_PACK_NAME, SPRITE_PACK_PATH, SPRITE_PACK_JSON);
    }

    @Test
    void testSpritePackSetup() {
        // testing getters for 100% coverage
        assertEquals(SPRITE_PACK_NAME, spritePack.getName(), "Comparing the names.");

        assertEquals(SPRITE_PACK_PATH, spritePack.getPath(), "Comparing the paths.");
    }

    @Test
    void testSpriteContent() {
        Map<String, Sprite> sprites = spritePack.getSprites();

        JSONObject jsonObject = JsonParser.stringToJSONObject(SPRITE_PACK_JSON);

        for (Map.Entry<String, Sprite> cSprite : sprites.entrySet()) {
            String name = cSprite.getKey();

            Sprite aSprite = Sprite.valueOf(name, SPRITE_PACK_PATH, jsonObject.getJSONObject(name));

            for (Animation animation : Animation.values()) {
                assertEquals(cSprite.getValue().getPaths(animation), aSprite.getPaths(animation),
                        "Comparing animations.");
            }

        }

    }

}
