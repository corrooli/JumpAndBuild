package ch.zhaw.pm3.teamretro.gamepack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class GamPackTest {

    static final String META_JSON = "{\"name\":\"Jump and Build\",\"levels\":[\"startLevel\"],\"spritePacks\":[\"castle\",\"day\",\"enemies\",\"levelelements\",\"protagonist\",\"snow\"]}";

    static final String META_NAME = "Jump and Build";

    static final Set<String> SPRITE_PACKS = new HashSet<>(
            Arrays.asList("castle", "day", "enemies", "levelelements", "protagonist", "snow"));

    static final List<String> LEVELS = Arrays.asList("startLevel");

    @Test
    void testCreatePack() {
        GamePack gamePack = GamePack.valueOf(META_JSON);
        assertEquals(META_NAME, gamePack.getName());

        assertEquals(LEVELS.size(), gamePack.getLevels().size());
        for (int i = 0; i < LEVELS.size(); i += 1) {
            assertEquals(LEVELS.get(i), gamePack.getLevels().get(i));
        }

        assertEquals(SPRITE_PACKS.size(), gamePack.getSpritePacks().size());
        for (String pack : SPRITE_PACKS) {
            assertTrue(gamePack.getSpritePacks().contains(pack));
        }
    }

}
