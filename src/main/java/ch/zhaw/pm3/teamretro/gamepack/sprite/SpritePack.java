package ch.zhaw.pm3.teamretro.gamepack.sprite;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONPropertyIgnore;

import ch.zhaw.pm3.teamretro.gamepack.JsonParser;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite.SpriteInformation;

/**
 * Represents a simple sprite pack.
 */
public class SpritePack {

	/**
	 * the sprite pack name
	 */
	private final String name;

	/**
	 * the path to the sprite pack
	 */
	private final String path;

	/**
	 * all the sprites inside of this sprite pack
	 */
	private final Map<String, Sprite> sprites;

	/**
	 * The sprite pack constructor is set to private by design, it will be created
	 * by the {@link #valueOf(String, String, String) valueOf}.
	 * 
	 * @param name    the sprite pack name
	 * @param path    the path to the sprite pack
	 * @param sprites the sprites belonging to this sprite pack
	 */
	private SpritePack(String name, String path, Map<String, Sprite> sprites) {
		this.name = name;
		this.path = path;
		this.sprites = sprites;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	@JSONPropertyIgnore
	public Sprite getSprite(String name) {
		return sprites.get(name);
	}

	/**
	 * This method will return an <a href=
	 * "https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Collections.html">unmodifiableMap</a>
	 * of all the sprite pack entries this sprite pack contains.
	 * 
	 * @return an unmodifiable map
	 */
	public Map<String, Sprite> getSprites() {
		return Collections.unmodifiableMap(sprites);
	}

	/**
	 * This function will initialize a SpritePack object with it's parameter filled
	 * up.
	 * 
	 * @param name the sprite name
	 * @param path the sprite location
	 * @param json the json to deserialize the content from
	 * @return a SpritePack object
	 */
	public static SpritePack valueOf(String name, String path, String json) {
		Map<String, Sprite> sprites = new HashMap<>();
		JSONObject jsonObj = JsonParser.stringToJSONObject(json);
		for (String spriteName : jsonObj.keySet()) {
			// replace the sprite names with the full ones
			SpriteInformation info = SpriteInformation.valueOf(name, spriteName);
			Sprite sprite = Sprite.valueOf(info.getFullSpriteName(), path, jsonObj.getJSONObject(spriteName));
			sprites.put(spriteName, sprite);
		}
		return new SpritePack(name, path, sprites);
	}
}
