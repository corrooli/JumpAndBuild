package ch.zhaw.pm3.teamretro.gamepack.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

class PositionTest {

	@Test
	void testValues() {
		double x = 0.0;
		double y = 10 / 3.0;
		Position pos = new Position(x, y);
		assertEquals(x, pos.getX());
		assertEquals(y, pos.getY());
		x = Double.MAX_VALUE;
		y = Double.MIN_VALUE;
		pos.setX(x);
		pos.setY(y);
		assertEquals(x, pos.getX());
		assertEquals(y, pos.getY());
	}

	@Test
	void testJSON() {
		JSONObject json = new JSONObject();
		json.put("x", "423.32");
		json.put("y", "-67.0");
		json.put("z", "far");
		Position pos = Position.valueOf(json);
		assertEquals(423.32, pos.getX());
		assertEquals(-67.0, pos.getY());

		JSONObject json2 = new JSONObject();
		json2.put("x", "100");
		json2.put("y", "0");
		json2.put("z", "deFault");
		Position pos2 = Position.valueOf(json2.toString());
		assertEquals(100, pos2.getX());
		assertEquals(0, pos2.getY());
	}

	@Test
	void testJSONNegative() {
		JSONObject json = new JSONObject();
		JSONException exception = assertThrows(JSONException.class, () -> Position.valueOf(json));
		assertEquals("JSONObject[\"x\"] not found.", exception.getMessage());

		json.put("x", "");
		json.put("y", "");
		exception = assertThrows(JSONException.class, () -> Position.valueOf(json));
		assertEquals("JSONObject[\"x\"] is not a double.", exception.getMessage());
	}

	@Test
	void testEquals() {
		double x = 4.32;
		double y = 123;
		Position pos = new Position(x, y);

		JSONObject json = new JSONObject();
		json.put("x", "4.32");
		json.put("y", "123");
		json.put("z", "near");
		Position posJ = Position.valueOf(json.toString());

		assertEquals(posJ, pos);
		assertEquals(pos, posJ);
		assertNotEquals(null, pos);
		assertNotEquals(new Position(1, 1), pos);
	}

	@Test
	void testHashCode() {
		double x = 4.32;
		double y = 123;
		Position pos = new Position(x, y);

		JSONObject json = new JSONObject();
		json.put("x", "4.32");
		json.put("y", "123");
		json.put("z", "near");
		Position posJ = Position.valueOf(json);

		assertEquals(pos.hashCode(), posJ.hashCode());

		pos.setX(55);
		posJ.setX(55);

		assertEquals(pos.hashCode(), posJ.hashCode());

		posJ.setY(-13);

		assertNotEquals(posJ.hashCode(), pos.hashCode());
	}

	@Test
	void testToString() {
		double x = 4.32;
		double y = 123;
		Position pos = new Position(x, y);

		JSONObject json = new JSONObject();
		json.put("x", "4.32");
		json.put("y", "123");
		json.put("z", "near");
		Position posJ = Position.valueOf(json);

		assertEquals("{x: 4.32, y: 123.00}", pos.toString());
		assertEquals("{x: 4.32, y: 123.00}", posJ.toString());

		pos.setX(0.12345679);
		pos.setY(10 / 3.0);

		assertEquals("{x: 0.12, y: 3.33}", pos.toString());
	}
}