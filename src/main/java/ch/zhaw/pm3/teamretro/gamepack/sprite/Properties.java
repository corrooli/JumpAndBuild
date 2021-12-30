package ch.zhaw.pm3.teamretro.gamepack.sprite;

import java.util.Objects;

import org.json.JSONObject;

import ch.zhaw.pm3.teamretro.gamepack.JsonParser;
import ch.zhaw.pm3.teamretro.gamepack.entity.Entity;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;

/**
 * Represents the different properties an {@link Entity} may have.
 * 
 * The default for the properties is based on the values coming from the
 * {@link Sprite}
 */
public class Properties {

	/**
	 * The representation of an {@link EntityType} inside of the json.
	 */
	private static final String ENTITY_TYPE_JSON = "entityType";

	/**
	 * The representation of an {@link Behavior} inside of the json.
	 */
	private static final String BEHAVIOR_JSON = "behavior";

	/**
	 * The representation of the solidity inside of the json.
	 */
	private static final String SOLIDITY_JSON = "solid";

	/**
	 * The {@link EntityType} type the property represents.
	 */
	private EntityType entityType = null;

	/**
	 * The {@link Behavior} type the property represents.
	 */
	private Behavior behavior = null;

	/**
	 * The solidity the property represents.
	 */
	private boolean solid = false;

	/**
	 * Constructs a property
	 * 
	 * @param behavior   the behavior
	 * @param entityType the type
	 * @param solid      the solidity
	 */
	public Properties(Behavior behavior, EntityType entityType, boolean solid) {
		this.entityType = entityType;
		this.behavior = behavior;
		this.solid = solid;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	public Behavior getBehavior() {
		return behavior;
	}

	public void setBehavior(Behavior behavior) {
		this.behavior = behavior;
	}

	public boolean isSolid() {
		return solid;
	}

	public void setSolid(boolean solid) {
		this.solid = solid;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null || !this.getClass().equals(other.getClass())) {
			return false;
		}
		Properties that = (Properties) other;
		return this.entityType == that.entityType && this.behavior == that.behavior && this.solid == that.solid;
	}

	@Override
	public int hashCode() {
		return Objects.hash(entityType, behavior, solid);
	}

	/**
	 * This function will parse the json and initialize a {@link Properties} object
	 * from it.
	 * 
	 * @param json the json to parse
	 * @return the generated object
	 */
	public static Properties valueOf(String json) {
		JSONObject jsonObj = JsonParser.stringToJSONObject(json);
		return valueOf(jsonObj);
	}

	/**
	 * This function will parse the json object and initialize a {@link Properties}
	 * object from it.
	 * 
	 * @param jsonObj the json object to parse
	 * @return the generated object
	 */
	public static Properties valueOf(JSONObject jsonObj) {
		EntityType entityType = EntityType.valueOf(jsonObj.optString(ENTITY_TYPE_JSON).toUpperCase());
		Behavior behavior = Behavior.valueOf(jsonObj.optString(BEHAVIOR_JSON).toUpperCase());
		boolean solid = jsonObj.optBoolean(SOLIDITY_JSON);

		return new Properties(behavior, entityType, solid);
	}
}
