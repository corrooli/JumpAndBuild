package ch.zhaw.pm3.teamretro.gamepack.sprite;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.zhaw.pm3.teamretro.gamepack.JsonParser;

class SpriteTest {
	List<Sprite> sprites;

	String jsonString = "{\"dirtSingle\": { \"name\": \"Dirt Single\", \"properties\": { \"entityType\": \"block\", \"behavior\": \"static\", \"solid\": true }, \"sprites\": { \"idle\": [ \"blocks/dirt_single.png\" ] } } }";
	String spriteName = "dirtSingle";
	String spritePath = "master/assets/sprites";

	@BeforeEach
	void setUp() {
		sprites = new ArrayList<>();
	}

	@Test
	void jsonMapping() {
		JSONObject json = JsonParser.stringToJSONObject(jsonString);
		sprites.add(Sprite.valueOf(spriteName, spritePath, json.getJSONObject(spriteName)));
		assertEquals(1, sprites.size(), "One Sprite expected after Test");
		Sprite actual = sprites.get(0);
		assertEquals("dirtSingle", actual.getName());
		assertEquals("Dirt Single", actual.getFancyName());

		Sprite expected = Sprite.valueOf(spriteName, spritePath, json.getJSONObject(spriteName).toString());

		assertEquals(expected.getFancyName(), actual.getFancyName(), "comparing fancy name");

		assertEquals(expected.getPaths(Animation.IDLE), actual.getPaths(Animation.IDLE), "comparing paths");

	}

}
