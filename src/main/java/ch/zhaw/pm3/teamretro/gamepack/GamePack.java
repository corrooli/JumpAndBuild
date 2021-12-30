package ch.zhaw.pm3.teamretro.gamepack;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPropertyIgnore;

/**
 * Represents the game-pack meta information.
 */
public class GamePack {
    /**
     * The name of the attribute in the json.
     */
    private static final String NAME_IN_JSON = "name";

    /**
     * The name of the level attribute in the json.
     */
    private static final String LEVELS_IN_JSON = "levels";

    /**
     * The name of the sprite pack information attribute in the json.
     */
    private static final String SPRITE_PACKS_IN_JSON = "spritePacks";

    /**
     * The current name of the given game pack
     */
    private String name;

    /**
     * all the levels inside of the game pack.
     */
    private final List<String> levels;

    /**
     * all the sprite packs inside of the game pack.
     */
    private final Set<String> spritePacks;

    public GamePack(String name, List<String> levels, Set<String> spritePacks) {
        this.name = name;
        this.levels = levels;
        this.spritePacks = spritePacks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLevels() {
        return Collections.unmodifiableList(levels);
    }

    /**
     * Will remove the given level from the gamePack data.
     * 
     * @param levelName
     * @return if the level was successfully removed
     */
    public boolean removeLevel(String levelName) {
        return levels.remove(levelName);
    }

    public void setLevels(List<String> levels) {
        this.levels.clear();
        this.levels.addAll(levels);
    }

    /**
     * Will add a new level entry to the list
     * 
     * @param levelName the name fo the new level
     * @param index     the index to insert the new level at (if -1 will append)
     */
    public void addLevel(String levelName, int index) {

        if (levels.contains(levelName)) {
            return;
        }

        if (index > levels.size() - 1) { // case that index too big
            index = -1; // just set it to append
        }
        if (index == -1) {
            levels.add(levelName);
        } else {
            levels.add(index, levelName);
        }
    }

    public Set<String> getSpritePacks() {
        return Collections.unmodifiableSet(spritePacks);
    }

    public void addSpritePack(String spritePack) {
        spritePacks.add(spritePack);
    }

    @JSONPropertyIgnore
    public String toJSON() {
        return JsonParser.objToJson(this);
    }

    /**
     * Will convert a JSONArray to a String list
     * 
     * @param arr
     * @return
     */
    @JSONPropertyIgnore
    private static List<String> convertToStringList(JSONArray arr) {
        return arr.toList().stream().map(e -> (String) e).collect(Collectors.toList());
    }

    /**
     * Will convert the json string to a correct {@link GamePack} object
     * 
     * @param json the meta data to parse
     * @return the {@link GamePack} obj
     */
    @JSONPropertyIgnore
    public static GamePack valueOf(String json) {

        JSONObject obj = JsonParser.stringToJSONObject(json);

        String name = obj.getString(NAME_IN_JSON);
        List<String> levels = convertToStringList(obj.getJSONArray(LEVELS_IN_JSON));
        Set<String> spritePacks = new HashSet<>(convertToStringList(obj.getJSONArray(SPRITE_PACKS_IN_JSON)));

        return new GamePack(name, levels, spritePacks);
    }
}
