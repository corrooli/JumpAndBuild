package ch.zhaw.pm3.teamretro.gamepack.entity;

import org.json.JSONObject;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;

/**
 * Represents a simple block in game.
 */
public class Block extends Entity {

	public Block(Position position, Sprite sprite, Properties properties) {
		super(EntityType.BLOCK, position, sprite, properties);
	}

	public Block(JSONObject jsonObject) throws InvalidLevelConfiguration {
		super(jsonObject);
	}

}
