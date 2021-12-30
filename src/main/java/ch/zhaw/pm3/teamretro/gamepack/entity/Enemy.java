package ch.zhaw.pm3.teamretro.gamepack.entity;

import org.json.JSONObject;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;

/**
 * Represents an enemy in the game.
 */
public class Enemy extends MovingEntity {

	public Enemy(Position position, Sprite sprite, Properties properties) {
		super(EntityType.ENEMY, position, sprite, properties);
	}

	public Enemy(JSONObject jsonObject) throws InvalidLevelConfiguration {
		super(jsonObject);
	}

}
