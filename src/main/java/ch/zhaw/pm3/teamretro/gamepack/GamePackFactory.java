package ch.zhaw.pm3.teamretro.gamepack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.zhaw.pm3.teamretro.archiveengine.ArchiveReader;
import ch.zhaw.pm3.teamretro.archiveengine.ArchiveWriter;
import ch.zhaw.pm3.teamretro.archiveengine.ZipReader;
import ch.zhaw.pm3.teamretro.archiveengine.ZipWriter;
import ch.zhaw.pm3.teamretro.gamepack.entity.Entity;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Animation;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Sprite.SpriteInformation;
import ch.zhaw.pm3.teamretro.gamepack.sprite.SpritePack;
import javafx.scene.image.Image;

/**
 * This is a factory class that handles any interaction with the into memory
 * loaded archive. It also achieves loading multiple versions of the given
 * archive at any time. Other then that it also helps to avoid the caller needed
 * to have any prior knowledge over the complex initialization process.
 */
public class GamePackFactory {

	/**
	 * The name of the meta information file inside of the game pack.
	 */
	private static final String META = "meta.json";

	/**
	 * The folder name for the levels.
	 */
	private static final String LEVELS_FOLDER = "levels";

	/**
	 * The path of the configurations.
	 */
	private static final String LEVEL_PATH = String.format("%s/%%s.json", LEVELS_FOLDER);

	/**
	 * The path for the sprite configurations.
	 */
	private static final String SPRITES_PATH = "assets/sprites/%s";

	/**
	 * The path to the meta information file of the given sprites.
	 */
	private static final String SPRITES_META = String.format("%s/%s", SPRITES_PATH, META);

	/**
	 * The archive reader, this static attribute is per design a singleton, as to
	 * avoid having the space required by the archive to be used multiple times.
	 */
	private static ArchiveReader archiveReader = null;

	/**
	 * This is empty so that there can not be an initialized version of this class.
	 */
	private GamePackFactory() {
	}

	/**
	 * Will setup the factory singleton.
	 * 
	 * Attention this needs to run first.
	 * 
	 * @param archiveName the archive path
	 * @throws IOException if something went wrong
	 */
	private static void setupFactory(String archiveName) throws IOException {
		if (archiveName == null) {
			return;
		}
		if (archiveReader != null && archiveName.equals(GamePackFactory.archiveReader.getName())) {
			return;
		}
		archiveReader = new ZipReader(archiveName);
	}

	/**
	 * Will setup the factory singleton.
	 *
	 * And will return a writer version of the given reader.
	 *
	 * Attention this will force a reload of all the singletons. And this assumes an
	 * existing archive.
	 *
	 * @param archiveName the archive path
	 * @return the archive writer path
	 * @throws IOException
	 */
	private static ArchiveWriter setupFactorWriter(String archiveName) throws IOException {
		ArchiveWriter archiveWriter = new ZipWriter(archiveName, true);
		// add a new updated reader instance
		archiveReader = archiveWriter;
		return archiveWriter;
	}

	/**
	 * Will read and parse the meta.json of the given game pack.
	 * 
	 * @param path the archive path
	 * @return the json representation of the meta.json
	 * @throws IOException
	 */
	public static GamePack getMetanformation(String path) throws IOException {
		setupFactory(path);
		String gamepackJson = archiveReader.getText(META);
		return GamePack.valueOf(gamepackJson);
	}

	/**
	 * Will get a list of levels from the archive meta data.
	 * 
	 * @param path the archive path
	 * @return the list of level names
	 * @throws IOException
	 */
	public static List<String> getLevelNames(String path) throws IOException {
		return getMetanformation(path).getLevels();
	}

	/**
	 * Will get a the list of sprite pack names from the archive meta data.
	 * 
	 * @param path the archive path
	 * @return the list of sprite pack names
	 * @throws IOException
	 */
	public static Set<String> getSpritePackNames(String path) throws IOException {
		return getMetanformation(path).getSpritePacks();
	}

	/**
	 * Will fill up the sprite with the needed images.
	 * 
	 * @param sprite the sprite to fill up
	 * @throws IOException if something went wrong
	 */
	private static void setSpriteData(Sprite sprite) throws IOException {
		// setup idle
		for (String path : sprite.getPaths(Animation.IDLE)) {
			Image img = new Image(archiveReader.getFile(path));

			sprite.addImage(Animation.IDLE, img);
		}
		// setup walk
		for (String path : sprite.getPaths(Animation.WALK)) {
			Image img = new Image(archiveReader.getFile(path));

			sprite.addImage(Animation.WALK, img);
		}
		// setup jumps
		for (String path : sprite.getPaths(Animation.JUMP)) {
			Image img = new Image(archiveReader.getFile(path));

			sprite.addImage(Animation.JUMP, img);
		}
	}

	/**
	 * Will add a new image to the given sprite
	 * 
	 * @param spritePacks all the initialized sprite packs
	 * @param sprites     all the sprite data
	 * @param spriteName  the name of the sprite to be initialized
	 * @throws IOException               if something went wrong with the
	 *                                   archiveEngine
	 * @throws InvalidLevelConfiguration
	 */
	private static void setImage(Map<String, SpritePack> spritePacks, Map<String, Sprite> sprites, String spriteName)
			throws IOException, InvalidLevelConfiguration {
		if (spritePacks.containsKey(spriteName)) {
			// sprite was already initializes return
			return;
		}
		// prepare the sprite information
		SpriteInformation spriteInformation = SpriteInformation.valueOf(spriteName);

		SpritePack spritePack = spritePacks.get(spriteInformation.getPackName());

		if (spritePack == null) {
			throw new InvalidLevelConfiguration(
					String.format("SpritePack <%s> does not exist", spriteInformation.getPackName()));
		}
		Sprite sprite = spritePack.getSprite(spriteInformation.getSpriteName());

		if (sprite == null) {
			throw new InvalidLevelConfiguration(
					String.format("Sprite <%s> does not exist", spriteInformation.getFullSpriteName()));
		}
		// add image data to sprite

		setSpriteData(sprite);
		sprites.put(spriteName, sprite);
	}

	/**
	 * This method will prepare the sprite packs and return the deserialized
	 * versions.
	 * 
	 * @param path
	 * @param spritePackNames
	 * @return
	 * @throws IOException
	 */
	private static Map<String, SpritePack> setupSpritePacks(String path, Set<String> spritePackNames)
			throws IOException {
		setupFactory(path);
		Map<String, SpritePack> spritePacks = new HashMap<>();
		// initialize all the sprite packs required
		for (String spriteName : spritePackNames) {
			String spritePath = String.format(SPRITES_PATH, spriteName);
			String spriteJson = archiveReader.getText(String.format(SPRITES_META, spriteName));
			SpritePack pack = SpritePack.valueOf(spriteName, spritePath, spriteJson);
			spritePacks.put(spriteName, pack);
		}

		return spritePacks;
	}

	/**
	 * Will generate a SpritePack representation of the given packages.
	 * 
	 * @param path            the archive path
	 * @param spritePackNames the name of the requested sprite-packs
	 * @return
	 * @throws IOException
	 */
	public static Map<String, Map<String, Sprite>> getSpritePacks(String path, Set<String> spritePackNames)
			throws IOException {
		setupFactory(path);

		Map<String, SpritePack> spritePacksData = setupSpritePacks(path, spritePackNames);

		Map<String, Map<String, Sprite>> spritePacks = new HashMap<>();
		for (String packName : spritePackNames) {
			SpritePack spritePack = spritePacksData.get(packName);
			Map<String, Sprite> sprites = spritePack.getSprites();

			for (Sprite sprite : sprites.values()) {
				setSpriteData(sprite);
			}
			spritePacks.put(packName, sprites);
		}
		return spritePacks;
	}

	/**
	 * Will get the level json from the archive.
	 * 
	 * This is set to package private so that it can be used from the testing
	 * environment.
	 * 
	 * @param archivePath the path to the archive
	 * @param levelName   the name of the levels
	 * @return the raw json string
	 * @throws IOException
	 */
	static String getLevelJson(String archivePath, String levelName) throws IOException {
		setupFactory(archivePath);
		return archiveReader.getText(String.format(LEVEL_PATH, levelName));
	}

	/**
	 * Will generate a level object and will fill it up with the needed sprite data
	 * 
	 * @param archivePath the archive path
	 * @param levelName   the name of the requested level
	 * @return a level object
	 * @throws IOException               if something went wrong with the archive
	 * @throws InvalidLevelConfiguration as the name says
	 */
	public static Level getLevel(String archivePath, String levelName) throws IOException, InvalidLevelConfiguration {
		// not calling setupFactory as it is called in getLevelJson anyway
		// initialize the level
		String levelJson = getLevelJson(archivePath, levelName);
		Level level = Level.valueOf(levelJson);
		setupLevel(archivePath, level);

		return level;
	}

	/**
	 * Will setup a level with it's content as requested.
	 * 
	 * @param archivePath the archive path
	 * @param level       the level to setup, needs a minimum on setup already done
	 * @throws IOException
	 * @throws InvalidLevelConfiguration
	 */
	private static void setupLevel(String archivePath, Level level) throws IOException, InvalidLevelConfiguration {
		Map<String, Sprite> sprites = new HashMap<>();
		Map<String, SpritePack> spritePacks = setupSpritePacks(archivePath, level.getSpritePacks());

		// add background
		String backgroundName = level.getBackgroundName();
		setImage(spritePacks, sprites, backgroundName);
		Sprite background = sprites.get(backgroundName);
		level.setBackground(background);

		// fill in sprites into entities
		for (Entity entity : level.getEntityList()) {
			String spriteName = entity.getSpriteName();
			setImage(spritePacks, sprites, spriteName);
			entity.setSprite(sprites.get(spriteName));
		}

	}

	/**
	 * Will create an empty initialized level.
	 * 
	 * Attention this level is not yet saved in the archive, until explicitly
	 * defined so.
	 * 
	 * @param archivePath
	 * @param levelName
	 * @return
	 * @throws IOException
	 * @throws InvalidLevelConfiguration
	 */
	public static Level getEmptyLevel(String archivePath, String levelName)
			throws IOException, InvalidLevelConfiguration {
		Level level = new Level(levelName);
		setupLevel(archivePath, level);
		return level;
	}

	/**
	 * This function will write a <b>new<b> level file into the given archive. This
	 * will append the level to the end of the level list.
	 *
	 * Attention this functionality assumes the archive already exists and is
	 * populated, with the corresponding file structures defined in the technical
	 * definitions.
	 *
	 * The important point is not to forget refreshing the ArchiveReader object, as
	 * it is the central connection between the archives and the rest of the
	 * implementation. The archive reader instance copies the full archive into
	 * memory to archive a speed up on an otherwise I/O bound computation. To force
	 * only a single copy in memory the ArchiveReader is implemented as suggested by
	 * the Singleton pattern. And is intended to be kept in such state.
	 *
	 * To overcome the limitation imposed by the required pattern of not resetting a
	 * new type during workflow, it is suggested to create a new ArchiveWriter
	 * object during write operations and overriding the previously instantiated
	 * Reader.
	 *
	 * @param archivePath
	 * @param levelName
	 * @param level
	 * @throws IOException
	 */
	public static void writeLevel(String archivePath, String levelName, Level level) throws IOException {
		writeLevel(archivePath, levelName, level, -1);
	}

	/**
	 * This function will write a <b>new<b> level file into the given archive.
	 *
	 * Attention this functionality assumes the archive already exists and is
	 * populated, with the corresponding file structures defined in the technical
	 * definitions.
	 *
	 * The important point is not to forget refreshing the ArchiveReader object, as
	 * it is the central connection between the archives and the rest of the
	 * implementation. The archive reader instance copies the full archive into
	 * memory to archive a speed up on an otherwise I/O bound computation. To force
	 * only a single copy in memory the ArchiveReader is implemented as suggested by
	 * the Singleton pattern. And is intended to be kept in such state.
	 *
	 * To overcome the limitation imposed by the required pattern of not resetting a
	 * new type during workflow, it is suggested to create a new ArchiveWriter
	 * object during write operations and overriding the previously instantiated
	 * Reader.
	 *
	 * @param archivePath the path to the archive
	 * @param levelName   the name of the level
	 * @param level
	 * @param index
	 * @throws IOException
	 */
	public static void writeLevel(String archivePath, String levelName, Level level, int index) throws IOException {

		ArchiveWriter archiveWriter = setupFactorWriter(archivePath);
		// append the level name to the list of previous written levels available
		GamePack gamePack = getMetanformation(archivePath);
		gamePack.addLevel(levelName, index);

		// write meta data back to the archive
		archiveWriter.addFile(gamePack.toJSON(), META);

		// build level path
		String levelPath = String.format(LEVEL_PATH, levelName);

		archiveWriter.addFile(level.toJson(), levelPath);

	}

	/**
	 * Will remove a single level from the game pack data.
	 *
	 * @param archivePath the archive path
	 * @param levelName   the level name
	 * @throws IOException
	 */
	public static void removeLevelData(String archivePath, String levelName) throws IOException {

		ArchiveWriter archiveWriter = setupFactorWriter(archivePath);

		GamePack gamePack = getMetanformation(archivePath);

		if (!gamePack.removeLevel(levelName)) {
			throw new IOException(
					String.format("The requested level <%s> can not be removed as it does not exist.", levelName));
		}

		archiveWriter.addFile(gamePack.toJSON(), META);

		String levelPath = String.format(LEVEL_PATH, levelName);

		archiveWriter.removeFile(levelPath);
	}

	/**
	 * Will setup an empty archive with preinstalled sprite packs.
	 * 
	 * @param sourceArchivePath      the source from which to copy the assets from
	 * @param destinationArchivePath where to to save the new archive to
	 * @param gamePack               the meta information needed for the game pack
	 * @throws IOException if something went wrong
	 */
	public static void setupArchive(String sourceArchivePath, String destinationArchivePath, GamePack gamePack)
			throws IOException {
		// copy from source path to destination

		// check if there already is an other archive at the given path.
		Path source = Paths.get(sourceArchivePath);
		Path destination = Paths.get(destinationArchivePath);
		if (Files.notExists(source)) {
			throw new IOException(String.format("Source file <%s> does not exist.", sourceArchivePath));
		}
		if (Files.exists(destination)) {
			throw new IOException(String.format("Destination file <%s> does already exist.", destinationArchivePath));
		}
		Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);

		ArchiveWriter archiveWriter = new ZipWriter(destinationArchivePath);

		// remove any old levels from the list
		GamePack orgPack = getMetanformation(sourceArchivePath);

		// remove all old level json definitions
		for (String oldLevel : orgPack.getLevels()) {
			// build level path
			String levelPath = String.format(LEVEL_PATH, oldLevel);
			archiveWriter.removeFile(levelPath);
			gamePack.removeLevel(oldLevel);
		}

		// create the meta data
		archiveWriter.addFile(gamePack.toJSON(), META);

		// write back the archive writer
		archiveReader = archiveWriter;
	}

}
