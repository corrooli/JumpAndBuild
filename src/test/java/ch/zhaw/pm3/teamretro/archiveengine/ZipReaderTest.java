package ch.zhaw.pm3.teamretro.archiveengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.lingala.zip4j.model.FileHeader;

class ZipReaderTest {

    static final String[] FILES_IN_ZIP = new String[] { "aDir/", "aDir/random.xml" };

    static final String RESOURCE_PATH = Paths.get("src", "test", "resources", "archiveengine").toString();

    static final String ZIP_NAME = Paths.get(RESOURCE_PATH, "aZip.zip").toAbsolutePath().toString();
    static final String FILENAME = Paths.get(RESOURCE_PATH, FILES_IN_ZIP[1]).toAbsolutePath().toString();

    static ZipReader zipReader;

    @BeforeAll
    static void setup() throws IOException {
        zipReader = new ZipReader(ZIP_NAME);
    }

    @Test
    void isValidZip() {
        assertTrue(zipReader.isValid());
    }

    @Test
    void testListFiles() throws IOException {
        Arrays.sort(FILES_IN_ZIP);
        List<FileHeader> files = zipReader.getFiles();

        assertEquals(FILES_IN_ZIP.length, files.size(), "comparing file lengths");

        for (int i = 0; i < files.size(); i++) {
            String base = Paths.get(FILES_IN_ZIP[i]).normalize().toString();
            String extr = Paths.get(files.get(i).toString()).normalize().toString();
            assertEquals(base, extr);
        }
    }

    private String inputStreamToString(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining(System.lineSeparator()));
    }

    @Test
    void testFileStream() throws IOException {
        try (FileInputStream readFile = new FileInputStream(new File(FILENAME));
                InputStream zipedFile = zipReader.getFile(FILES_IN_ZIP[1]);) {

            String fileText = inputStreamToString(zipedFile);
            String orgText = inputStreamToString(readFile);
            assertEquals(orgText, fileText);
        }
    }

    @Test
    void testDirectoryException() {
        IOException actual = assertThrows(IOException.class, () -> {
            String dirName = FILES_IN_ZIP[0];
            zipReader.getFile(dirName);
        });

        final String expected = "No directories may be extracted from this method.";
        assertEquals(expected, actual.getMessage());
    }

    @Test
    void testHasFile() throws IOException {
        assertTrue(zipReader.hasFile(FILES_IN_ZIP[1]));
    }

    @Test
    void testNotHasFile() throws IOException {
        assertFalse(zipReader.hasFile("aRandomNameForAFile.txt"));
    }

    @Test
    void testIsDirectoryInsteadOfFile() throws IOException {
        assertFalse(zipReader.hasFile(FILES_IN_ZIP[0]));
    }

    @Test
    void testHasDirectory() throws IOException {
        assertTrue(zipReader.hasFolder(FILES_IN_ZIP[0]));
    }

    @Test
    void testNotHasDirectory() throws IOException {
        assertFalse(zipReader.hasFolder("aRandomNameForADirectory/"));
    }

    @Test
    void testIsFileInsteadOfDirectory() throws IOException {
        assertFalse(zipReader.hasFolder(FILES_IN_ZIP[1]));
    }
}
