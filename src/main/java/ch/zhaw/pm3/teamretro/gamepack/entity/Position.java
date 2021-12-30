package ch.zhaw.pm3.teamretro.gamepack.entity;

import java.util.Objects;

import org.json.JSONObject;

import ch.zhaw.pm3.teamretro.gamepack.JsonParser;

/**
 * Contains the Position as double x and y
 */
public class Position {

	/**
	 * Is the block spacing of the pixels on the grid.
	 */
	private static final double GRID_SPACING = 32.0;

	private double x;
	private double y;

	/**
	 * Create new Position
	 *
	 * @param x x
	 * @param y y
	 */
	public Position(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Clamps a given coordinate to the nearest lower multiple of 32 (corresponding
	 * to block grid).
	 *
	 * @return Clamped position.
	 */
	public Position clamp() {
		return new Position(Position.clampToGrid(x), Position.clampToGrid(y));
	}

	/**
	 * Helper method, clamps a given coordinate to the nearest lower multiple of 32
	 * (corresponding to block grid)
	 *
	 * @param position Position to be clamped.
	 * @return Clamped position.
	 */
	private static double clampToGrid(double position) {
		return position - position % GRID_SPACING;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Position position = (Position) o;
		return Double.compare(position.x, x) == 0 && Double.compare(position.y, y) == 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return String.format("{x: %.2f, y: %.2f}", x, y);
	}

	/**
	 * Creates a new Position from the given json data.
	 *
	 * @param json JSON Object
	 */
	public static Position valueOf(String json) {
		JSONObject jObject = JsonParser.stringToJSONObject(json);
		return valueOf(jObject);
	}

	/**
	 * Create new Position from JSON Object
	 *
	 * @param jsonObject JSON Object
	 */
	public static Position valueOf(JSONObject jsonObject) {
		double x = jsonObject.getDouble("x");
		double y = jsonObject.getDouble("y");
		return new Position(x, y);
	}
}
