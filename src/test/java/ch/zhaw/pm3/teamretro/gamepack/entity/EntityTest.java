package ch.zhaw.pm3.teamretro.gamepack.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Animation;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;

class EntityTest {

	Entity entity;
	Sprite sprite;

	@BeforeEach
	void setUp() throws InvalidLevelConfiguration {
		// using mockito to not have to create and fill up the json object
		sprite = mock(Sprite.class);
		when(sprite.getImages(Animation.IDLE)).thenReturn(Arrays.asList());
		// String json = "{\"sprite\": \"levelements.goalPost\",\"position\": {\"x\":
		// 160,\"y\": 578,\"z\": \"default\"}},";
		JSONObject obj = new JSONObject();
		obj.put("entityType", "block");
		JSONObject properties = new JSONObject();
		properties.put("entityType", "block");
		properties.put("behavior", "win");
		properties.put("solid", true);
		obj.put("properties", properties);
		obj.put("sprite", "levelements.goalPost");
		JSONObject position = new JSONObject();
		position.put("x", "160");
		position.put("y", "578");
		obj.put("position", position);
		entity = new Block(obj);
	}

	@Test
	void testGetNextImage() throws InvalidLevelConfiguration {
		assertThrows(InvalidLevelConfiguration.class, () -> entity.getNextImage(Animation.IDLE));
		assertThrows(InvalidLevelConfiguration.class, () -> entity.getCurrentImage());
	}

	@Test
	void testFlipped() {
		// 100% converage
		assertFalse(entity.isFlipped());
		entity.toggleFlipped();
		assertTrue(entity.isFlipped());
		entity.setFlipped(true);
		assertTrue(entity.isFlipped());
		entity.setFlipped(false);
		assertFalse(entity.isFlipped());
	}

	@Test
	void testEntityType() {
		assertEquals(EntityType.BLOCK, entity.getEntityType());
		assertEquals("BLOCK", entity.getProperties().getEntityType().name());
	}

	@Test
	void testPosition() {
		assertEquals(new Position(160, 578), entity.getPosition());
		Position p = new Position(1.2, 1.2);
		entity.setPosition(p);
		assertEquals(p, entity.getPosition());
	}

	@Test
	void testSpriteName() {
		assertEquals("levelements.goalPost", entity.getSpriteName());
	}

	@Test
	void testProperties() {
		JSONObject properties = new JSONObject();
		properties.put("entityType", "block");
		properties.put("behavior", "win");
		properties.put("solid", true);
		Properties p = Properties.valueOf(properties.toString());
		assertEquals(p, entity.getProperties());
	}
}