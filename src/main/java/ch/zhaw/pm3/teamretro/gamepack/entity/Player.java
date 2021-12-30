package ch.zhaw.pm3.teamretro.gamepack.entity;

import org.json.JSONObject;
import org.json.JSONPropertyIgnore;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Behavior;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;

/**
 * Represents a player inside of the game.
 */
public class Player extends MovingEntity {

	/**
	 * The current score of the player.
	 */
	private int score = 0;

	/**
	 * The current vulnerability status.
	 */
	private boolean vulnerable = true;

	public Player() {
		super(EntityType.PLAYER, new Position(128, 128), new Sprite("protagonist.protagonist"),
				new Properties(Behavior.PLAYABLE, EntityType.PLAYER, true));
	}

	public Player(Position position, Sprite sprite, Properties properties) {
		super(EntityType.PLAYER, position, sprite, properties);
	}

	public Player(JSONObject jsonObject) throws InvalidLevelConfiguration {
		super(jsonObject);
	}

	public void updateScore(int amount) {
		score += amount;
	}

	@JSONPropertyIgnore
	public int getScore() {
		return score;
	}

	public void setScore(int amount) {
		score = amount;
	}

	public void makeInvulnerable() {
		vulnerable = false;
	}

	@JSONPropertyIgnore
	public boolean getVunerability() {
		return vulnerable;
	}

}
