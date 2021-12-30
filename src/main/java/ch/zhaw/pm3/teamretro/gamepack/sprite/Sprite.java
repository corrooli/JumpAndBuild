package ch.zhaw.pm3.teamretro.gamepack.sprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPropertyName;

import ch.zhaw.pm3.teamretro.gamepack.JsonParser;
import ch.zhaw.pm3.teamretro.gamepack.entity.EntityType;
import javafx.scene.image.Image;

/**
 * Represents a single sprite data package. This means that is contains
 * different images for different animations.
 */
public class Sprite {

	/**
	 * Is the formatter delimiter used to combine the sprite path and the image
	 * name.
	 */
	private static final String PATH_DELIMITER = "%s/%s";

	/**
	 * The fancy name as set by the json.
	 */
	private static final String NAME_JSON = "name";

	/**
	 * The sprite information as set by the json.
	 */
	private static final String SPRITE_PACKS_JSON = "sprites";

	/**
	 * The idle sprite information as set by the json.
	 */
	private static final String IDLE_SPRITES_JSON = "idle";

	/**
	 * The walk sprite information as set by the json.
	 */
	private static final String WALK_SPRITES_JSON = "walk";

	/**
	 * The jump sprite information as set by the json.
	 */
	private static final String JUMP_SPRITES_JSON = "jump";

	/**
	 * The properties information as set by the json.
	 */
	private static final String PROPERTIES_JSON = "properties";

	/**
	 * Is the internal name of the given sprite. See {@link SpriteInformation}
	 */
	private final String name;

	/**
	 * The fancy and more printable name of the sprite.
	 */
	private final String fancyName;

	/**
	 * The set properties of the sprite.
	 */
	private final Properties properties;

	/**
	 * The images used for the idle animation.
	 */
	private final List<Image> idle = new ArrayList<>();

	/**
	 * The images used for the walking animation.
	 */
	private final List<Image> walk = new ArrayList<>();

	/**
	 * The images used for the jumping animation.
	 */
	private final List<Image> jump = new ArrayList<>();

	/**
	 * The paths to the images used for the idle animation.
	 */
	private final List<String> idlePaths = new ArrayList<>();

	/**
	 * The paths to the images used for the walking animation.
	 */
	private final List<String> walkPaths = new ArrayList<>();

	/**
	 * The paths to the images used for the jumping animation.
	 */
	private final List<String> jumpPaths = new ArrayList<>();

	/**
	 * Creates a default sprite with only the name needed.
	 * 
	 * @param sprite the sprite name
	 */
	public Sprite(String sprite) {
		this.name = sprite;
		this.fancyName = sprite;
		this.properties = new Properties(Behavior.STATIC, EntityType.BLOCK, true);
	}

	/**
	 * Creates the actual sprite with all the information required.
	 * 
	 * Attention this constructor is set to private by design, as it is supposed to
	 * be called via the one of the {@link #valueOf} methods.
	 * 
	 * @param name the sprite name that was loaded
	 * @param json the deserialized json data
	 * @param path the path to the sprite images
	 */
	private Sprite(String name, JSONObject json, String path) {
		this.name = name;
		this.fancyName = json.optString(NAME_JSON, name);

		JSONObject sprites = json.getJSONObject(SPRITE_PACKS_JSON);
		JSONArray idleArray = sprites.optJSONArray(IDLE_SPRITES_JSON);
		JSONArray walkArray = sprites.optJSONArray(WALK_SPRITES_JSON);
		JSONArray jumpArray = sprites.optJSONArray(JUMP_SPRITES_JSON);

		if (idleArray != null) {
			for (Object imgPath : idleArray) {
				idlePaths.add(String.format(PATH_DELIMITER, path, (String) imgPath));
			}
		}
		if (walkArray != null) {
			for (Object imgPath : walkArray) {
				walkPaths.add(String.format(PATH_DELIMITER, path, (String) imgPath));
			}
		}
		if (jumpArray != null) {
			for (Object imgPath : jumpArray) {
				jumpPaths.add(String.format(PATH_DELIMITER, path, (String) imgPath));
			}
		}
		JSONObject propertiesJSON = json.optJSONObject(PROPERTIES_JSON);
		if (propertiesJSON != null) {
			this.properties = Properties.valueOf(propertiesJSON);
		} else {
			// the default properties
			this.properties = new Properties(Behavior.STATIC, EntityType.BLOCK, true);
		}
	}

	public Properties getProperties() {
		return properties;
	}

	public String getName() {
		return name;
	}

	@JSONPropertyName(NAME_JSON)
	public String getFancyName() {
		return fancyName;
	}

	/**
	 * Will return an unmodifiable list with the given paths.
	 * 
	 * @param animation the type of paths list to return
	 * @return a list a list of Strings
	 */
	public List<String> getPaths(Animation animation) {
		switch (animation) {
			case IDLE:
				return Collections.unmodifiableList(idlePaths);
			case JUMP:
				return Collections.unmodifiableList(jumpPaths);
			case WALK:
				return Collections.unmodifiableList(walkPaths);
			default:
				throw new EnumConstantNotPresentException(Animation.class, animation.toString());
		}
	}

	/**
	 * Will add an Image to the corresponding list.
	 * 
	 * @param animation
	 * @param image
	 */
	public void addImage(Animation animation, Image image) {
		getImagesHelper(animation).add(image);
	}

	/**
	 * does the switch case for all the image list returning code.
	 * 
	 * @param animation the animation type to return
	 * @return a modifiable list
	 */
	private List<Image> getImagesHelper(Animation animation) {
		switch (animation) {
			case IDLE:
				return idle;
			case JUMP:
				return jump;
			case WALK:
				return walk;
			default:
				throw new EnumConstantNotPresentException(Animation.class, animation.toString());
		}
	}

	/**
	 * Will return all the images from the given type.
	 * 
	 * @param animation the type of animation to return
	 * @return an unmodifiable list
	 */
	public List<Image> getImages(Animation animation) {
		return Collections.unmodifiableList(getImagesHelper(animation));
	}

	public SpriteInformation getSpriteInformation() {
		return SpriteInformation.valueOf(name);
	}

	/**
	 * Will generate a new sprite from the json string.
	 * 
	 * @param name the name of the sprite
	 * @param path the path to the sprite pack
	 * @param json the json containing all the data
	 * @return the sprite
	 */
	public static Sprite valueOf(String name, String path, String json) {
		return valueOf(name, path, JsonParser.stringToJSONObject(json));
	}

	/**
	 * Will generate a new sprite from the json object.
	 * 
	 * @param name the name of the sprite
	 * @param path the path to the sprite pack
	 * @param json the json containing all the data
	 * @return the sprite
	 */
	public static Sprite valueOf(String name, String path, JSONObject json) {
		return new Sprite(name, json, path);
	}

	/**
	 * This class is used to split up the sprite name into it's components.
	 */
	public static class SpriteInformation {

		/**
		 * the delimiter between the sprite name and the actual sprite name
		 */
		private static final String DELIMITER = ".";

		/**
		 * The regex used to split up the initial name.
		 */
		private static final String DELIMITER_REGEX = "\\.";

		/**
		 * The location relative where the sprite pack name resides in the initial given
		 * parameter.
		 */
		private static final int SPRITE_PACK_NAME = 0;

		/**
		 * The location relative where the sprite name itself resides in the initial
		 * given parameter.
		 */
		private static final int SPRITE_NAME = 1;

		/**
		 * The sprite pack name of this current object.
		 */
		private final String packName;

		/**
		 * The sprite name of this current object.
		 */
		private final String spriteName;

		/**
		 * Initializes the object, this is set to private by design as the object gets
		 * created by {@link #valueOf(String) valueOf}.
		 * 
		 * @param spritePath the sprite path
		 */
		private SpriteInformation(String spritePath) {
			String[] spritePathParts = spritePath.split(DELIMITER_REGEX);
			this.packName = spritePathParts[SPRITE_PACK_NAME];
			this.spriteName = spritePathParts[SPRITE_NAME];
		}

		/**
		 * Initializes the object, this is set to private by design as the object gets
		 * created by {@link #valueOf(String, String) valueOf}.
		 * 
		 * @param spritePath the sprite path
		 */
		private SpriteInformation(String packName, String spriteName) {
			this.packName = packName;
			this.spriteName = spriteName;
		}

		/**
		 * Will return a sprite information object from it's path
		 * 
		 * @param spritePath the path to split
		 * @return a initialized obj
		 */
		public static SpriteInformation valueOf(String spritePath) {
			return new SpriteInformation(spritePath);
		}

		/**
		 * Will create sprite information object from it's component
		 * 
		 * @param packName   the pack name
		 * @param spriteName the sprite name
		 * @return a initialized obj
		 */
		public static SpriteInformation valueOf(String packName, String spriteName) {
			return new SpriteInformation(packName, spriteName);
		}

		public String getPackName() {
			return packName;
		}

		public String getSpriteName() {
			return spriteName;
		}

		/**
		 * Will return a json ready sprite path
		 * 
		 * @return the full sprite path
		 */
		public String getFullSpriteName() {
			return String.join(DELIMITER, Arrays.asList(packName, spriteName));
		}
	}
}
