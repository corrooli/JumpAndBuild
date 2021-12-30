package ch.zhaw.pm3.teamretro.archiveengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;

/**
 * This class is meant as a pure abstraction layer back-end intended as
 * interface to the zip4j framework. It should only be used in this package per
 * design.
 */
public class ZipReader implements ArchiveReader {

	/**
	 * The default delimiter used by the current archive delimiter.
	 */
	protected static final String ARCHIVE_DELIMITER = "/";

	/**
	 * Not a correct delimiter for the paths, will replaced by the
	 * ARCHIVE_DELIMITER.
	 */
	protected static final String NOT_ARCHIVE_DELIMITER = "\\\\";

	/**
	 * The zip file name / location
	 */
	protected final String name;

	/**
	 * The main zip data saved in memory
	 */
	protected final ZipFile zipFile;

	/**
	 * Constructs an archive reader object for an zip file, this constructor assumes
	 * a valid system.
	 * 
	 * @param fileName the name of the zip archive
	 * @throws IOException if something went wrong
	 */
	public ZipReader(String fileName) throws IOException {
		this(fileName, true);
	}

	/**
	 * Constructs an archive reader object for an zip file, this constructor gives
	 * the choice if the zip already exists or not.
	 *
	 * Attention this constructor may only be called internally, that is why it is
	 * set to protected.
	 * 
	 * @param fileName  the name of the zip archive
	 * @param fileExist true if the zip already has to exist false if not
	 * @throws IOException if something went wrong
	 */
	protected ZipReader(String fileName, boolean fileExist) throws IOException {
		this.name = fileName;
		this.zipFile = new ZipFile(fileName);

		if (fileExist) {
			if (!zipFile.getFile().exists()) {
				throw new IOException(String.format("The requested file %s does not exist.", this.name));
			}

			if (!isValid()) {
				throw new IOException(
						String.format("The requested file %s is no valid zip file. or doesn't exist.", this.name));
			}
		}
	}

	/**
	 * Will check if the given zip file is valid.
	 * 
	 * @return true if is valid false otherwise
	 */
	public boolean isValid() {
		return zipFile.isValidZipFile() && zipFile.getFile().exists();
	}

	/**
	 * Will return the needed file headers for the given file.
	 * 
	 * @param filename the requested file name
	 * @return the decompressed file header
	 * @throws IOException if the file name doesn't exist
	 */
	protected FileHeader getFileHeader(String filename) throws IOException {
		return zipFile.getFileHeader(filename);
	}

	/**
	 * Will return an overview of all the files in the zip folder.
	 * 
	 * @return a list of all the files inside the archive
	 * @throws IOException if the archive is corrupt or other
	 */
	public List<FileHeader> getFiles() throws IOException {
		return zipFile.getFileHeaders();
	}

	@Override
	public String getText(String filename) throws IOException {
		return getText(filename, StandardCharsets.UTF_8);
	}

	@Override
	public String getText(String filename, Charset charset) throws IOException {
		InputStream stream = getFile(filename);
		InputStreamReader isr = new InputStreamReader(stream, charset);
		return new BufferedReader(isr).lines().collect(Collectors.joining(System.lineSeparator()));
	}

	/**
	 * Will extract the file from the zip and present the data as an InputStream
	 * 
	 * @param fileName the file path to be extracted
	 * @return an input stream of the extracted
	 * @throws IOException will be thrown assuming the searched for file doesn't
	 *                     exist or
	 */
	@Override
	public InputStream getFile(String fileName) throws IOException {
		if (hasFolder(fileName)) {
			throw new IOException("No directories may be extracted from this method.");
		}

		if (!hasFile(fileName)) {
			throw new IOException(String.format("No such file exists <%s>", fileName));
		}

		FileHeader fh = getFileHeader(fileName);

		return zipFile.getInputStream(fh);
	}

	@Override
	public boolean hasFile(String filename) {
		FileHeader fh = null;
		try {
			fh = getFileHeader(filename);
		} catch (IOException e) {
			// there is no need to do anything
			// with this exception as we only
			// care if the header was set or
			// not
		}
		return fh != null && !fh.isDirectory();
	}

	@Override
	public boolean hasFolder(String dirname) {
		FileHeader fh = null;
		try {
			fh = getFileHeader(dirname);
		} catch (IOException e) {
			// there is no need to do anything
			// with this exception as we only
			// care if the header was set or
			// not
		}
		return fh != null && fh.isDirectory();
	}

	/**
	 * Will check if there is a parent folder or not.
	 * 
	 * This is will return true if parent is null, as that means the entry shall be
	 * added to the archive root.
	 * 
	 * @param path the parent folder path
	 * @return if the folder exists
	 */
	protected boolean hasParentFolder(String path) {
		Path parentPath = Path.of(path);
		Path parentDir = parentPath.getParent();
		// can be added as it's in the top level
		return parentDir == null || hasFolder(pathToZipPath(parentDir.toString()) + ARCHIVE_DELIMITER);
	}

	/**
	 * Will convert a path to a correct Zip Path as a String.
	 * 
	 * @param filePath the path to convert
	 * @return the converted and corrected path
	 */
	protected String pathToZipPath(String filePath) {
		return Path.of(filePath).normalize().toString().replace(NOT_ARCHIVE_DELIMITER, ARCHIVE_DELIMITER);
	}

	@Override
	public String getName() {
		return name;
	}
}