package ch.zhaw.pm3.teamretro.archiveengine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ZipWriterTest {
	static final String[] FILES_IN_ZIP = new String[] { "aDir/", "random.xml" };

	static final String RESOURCE_PATH = Paths.get("src", "test", "resources", "archiveengine").toAbsolutePath()
			.toString();

	static final String ZIP_NAME = Paths.get(RESOURCE_PATH, "aZip.zip").toAbsolutePath().toString();
	static final String FILENAME = Paths.get(RESOURCE_PATH, FILES_IN_ZIP[0], FILES_IN_ZIP[1]).toAbsolutePath()
			.toString();
	static String randomFileContent;

	// Specify some temporary files.
	static final String prefix = "tmpArchive.zip";
	Path tempDir;
	String zipFilePath;

	ZipWriter zw;

	@BeforeAll
	static void readRandomFileContent() throws IOException {
		randomFileContent = String.join(System.lineSeparator(), Files.readAllLines(Path.of(FILENAME)));
	}

	@BeforeEach
	void setup() throws IOException {
		setupArchive();
	}

	void setupArchive() throws IOException {
		tempDir = Files.createTempDirectory(prefix);
		zipFilePath = Paths.get(tempDir.toString(), prefix).toString();
		zw = new ZipWriter(zipFilePath);
	}

	private static boolean deleteDirectory(File directoryToBeDeleted) {
		File[] allContents = directoryToBeDeleted.listFiles();
		if (allContents != null) {
			for (File file : allContents) {
				deleteDirectory(file);
			}
		}
		return directoryToBeDeleted.delete();
	}

	@AfterEach
	void cleanUpArchive() throws IOException {
		// force push the archive reader on to the garbarge collector
		deleteDirectory(tempDir.toFile());
	}

	InputStream getStream(String str) {
		return new ByteArrayInputStream(str.getBytes());
	}

	@Test
	void testAddFile() throws IOException {
		// add file to zip file
		InputStream is = getStream(randomFileContent);
		zw.addFile(is, FILES_IN_ZIP[1]);

		ArchiveReader ar = new ZipReader(zipFilePath);
		// assert text from zip is equals to orignal
		final String zipedContent = ar.getText(FILES_IN_ZIP[1]);
		assertEquals(randomFileContent, zipedContent);
	}

	@Test
	void testAddFileDirectlyIntoFolder() throws IOException {
		final String internalFilepath = Paths.get(FILES_IN_ZIP[0], FILES_IN_ZIP[1]).toString();
		// add file to zip file
		InputStream is = getStream(randomFileContent);
		assertThrows(IOException.class, () -> zw.addFile(is, internalFilepath));
		assertThrows(IOException.class, () -> new ZipReader(zipFilePath));
	}

	@Test
	void testAddFolder() throws IOException {
		zw.addFolder(FILES_IN_ZIP[0]);

		ArchiveReader ar = new ZipReader(zipFilePath);
		assertTrue(ar.hasFolder(FILES_IN_ZIP[0]));
	}

	private String[] removePath = null;
	private String[] removePathFiles = null;

	private void setupRemove() throws IOException {
		removePath = new String[] { Path.of("aRandomPath").toString() + "/",
				Path.of("aRandomPath", "anOtherRandomPath").toString() + "/" };

		removePathFiles = new String[] { Path.of(removePath[0], FILES_IN_ZIP[1]).toString(),
				Path.of(removePath[1], FILES_IN_ZIP[1]).toString() };

		for (String path : removePath) {
			zw.addFolder(path);
		}

		for (String path : removePathFiles) {
			zw.addFile(randomFileContent, path);
		}
	}

	@Test
	void testRemoveFileDirect() throws IOException {
		setupRemove();

		zw.removeFile(removePathFiles[0]);

		ArchiveReader ar = new ZipReader(zipFilePath);
		assertFalse(ar.hasFile(removePathFiles[0]));
	}

	@Test
	void testRemoveFileIndirect() throws IOException {
		setupRemove();

		zw.removeFile(removePathFiles[0]);

		ArchiveReader ar = new ZipReader(zipFilePath);
		assertFalse(ar.hasFile(removePathFiles[0]));
	}

	@Test
	void testRemoveFolderDirect() throws IOException {
		zw.addFolder(FILES_IN_ZIP[0]);

		ArchiveReader ar = new ZipReader(zipFilePath);
		assertTrue(ar.hasFolder(FILES_IN_ZIP[0]));

		zw.removeFolder(FILES_IN_ZIP[0]);
		ar = new ZipReader(zipFilePath);
		assertFalse(ar.hasFolder(FILES_IN_ZIP[0]));
	}

	@Test
	void testRemoveFolderIndirect() throws IOException {
		setupRemove();
		zw.removeFolder(removePath[1]);

		ArchiveReader ar = new ZipReader(zipFilePath);

		assertFalse(ar.hasFolder(removePathFiles[1]));
	}
}
