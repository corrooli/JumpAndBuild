package ch.zhaw.pm3.teamretro.gamepack.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Properties;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;

/**
 * Represents an item in game.
 */
public class Item extends Entity {

	// list that contains all possible effects for given item
	private List<Effect> effectList = new ArrayList<>();

	public Item(Position position, Sprite sprite, Properties prop) {
		super(EntityType.ITEM, position, sprite, prop);
	}

	public Item(JSONObject jsonObject) throws InvalidLevelConfiguration {
		super(jsonObject);
		for (Object rawEffect : jsonObject.getJSONArray("effects")) {
			String effect = (String) rawEffect;
			effectList.add(Effect.valueOf(effect.toUpperCase()));
		}
	}

	/**
	 * Will return the effects that this item has.
	 * 
	 * @return the effects of this item
	 */
	public List<Effect> getEffect() {
		return Collections.unmodifiableList(effectList);
	}

	/**
	 * Affect the player by the item's effect property.
	 *
	 * @param effect Each item has an effect on the player that touches it.
	 */
	public void applyEffect(Effect effect) {
		switch (effect) {
			case SCORE:
				increaseScore();
				break;
			case SPEED:
				increaseSpeed();
				break;
			case INVULNERABLE:
				makeInvulnerable();
				break;
			default:
				throw new EnumConstantNotPresentException(Effect.class, effect.name());

		}
	}

	/**
	 * Increase the score of the current player.
	 */
	private void increaseScore() {
		// todo
	}

	/**
	 * Increase the speed of the current player.
	 */
	private void increaseSpeed() {
		// todo
	}

	/**
	 * Make player move through enemies without getting hurt.
	 */
	private void makeInvulnerable() {
		// todo
	}
}
