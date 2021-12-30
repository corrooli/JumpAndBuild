package ch.zhaw.pm3.teamretro.gamepack.sprite;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesTest {

	static Properties prop, prop2;

	@BeforeEach
	void setupTests() {
		JSONObject obj = new JSONObject();
		prop = new Properties(Behavior.GENERICENEMY, EntityType.ENEMY, true);
		prop.setEntityType(EntityType.ENEMY);
		prop.setBehavior(Behavior.STATIC);
		prop.setSolid(true);
		obj.put("entityType", "enemy");
		obj.put("solid", "true");
		obj.put("behavior", "static");
		prop2 = Properties.valueOf(obj.toString());
	}

	@Test
	void testEquals() {
		// test the implementation if the same object will be checked as equals
		assertEquals(prop, prop);
		// test if the implementation understands if the content is the same
		assertEquals(prop, prop2);
	}

	@Test
	void testNotEquals() {
		// test if the implementation understands if second object is null
		assertNotEquals(null, prop);
		// test if the implementation understands if second object is of an other type
		assertNotEquals("I am a property.", prop);

		// change some parts of prop2 behavior
		prop2.setBehavior(Behavior.DAMAGE);
		assertNotEquals(prop, prop2);
		prop2.setBehavior(prop.getBehavior());

		// entity
		prop2.setEntityType(EntityType.BACKGROUND);
		assertNotEquals(prop, prop2);
		prop2.setEntityType(prop.getEntityType());

		// solidity
		prop2.setSolid(false);
		assertNotEquals(prop, prop2);
	}

	@Test
	void testHashCode() {
		assertEquals(prop.hashCode(), prop2.hashCode());
	}
}