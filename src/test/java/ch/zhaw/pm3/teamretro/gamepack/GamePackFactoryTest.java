package ch.zhaw.pm3.teamretro.gamepack;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class GamePackFactoryTest {
    static final String MASTER_PATH = Paths.get("src", "test", "resources", "master.zip").toString();

    @Test
    void testGetLevelNames() throws IOException {
        List<String> names = GamePackFactory.getLevelNames(MASTER_PATH);

        List<String> namesExpected = Arrays.asList("startLevel");

        assertEquals(namesExpected.size(), names.size());

        for (int i = 0; i < namesExpected.size(); i += 1) {
            assertEquals(namesExpected.get(i), names.get(i));
        }
    }

    @Test
    void testGetLevelNamesMult() throws IOException {
        testGetLevelNames();
        testGetLevelNames();
        testGetLevelNames();
    }

    @Test
    void testGetLevel() throws IOException, InvalidLevelConfiguration {
        Level level = GamePackFactory.getLevel(MASTER_PATH, "startLevel");
        // check if the deserializaiton worked correctly
        String levelJsonExp = GamePackFactory.getLevelJson(MASTER_PATH, "startLevel");
        String levelJsonArc = level.toJson();
        JSONAssert.assertEquals(levelJsonArc, levelJsonExp, false);
    }
}
