package ch.zhaw.pm3.teamretro.archiveengine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;

/**
 * This class is meant as a pure abstraction layer back-end intended as
 * interface to the zip4j framework. It should only be used in this package per
 * design.
 */
public class ZipWriter extends ZipReader implements ArchiveWriter {

	/**
	 * Will create a new zip oriented ArchiveWriter.
	 * 
	 * This class accepts a path to a not existing zip file, it will be generated,
	 * once a folder or file is added to the archive.
	 * 
	 * @param fileName the name of the archive.
	 * @throws IOException if the file doesn't exist or other
	 */
	public ZipWriter(String fileName) throws IOException {
		this(fileName, false);
	}

	/**
	 * Will create a new zip oriented ArchiveWriter.
	 * 
	 * This class accepts a path to a not existing zip file, it will be generated,
	 * once a folder or file is added to the archive.
	 * 
	 * @param fileName   the name of the archive.
	 * @param fileExists checks during initialization if the archive exists.
	 * @throws IOException if the file doesn't exists or other
	 */
	public ZipWriter(String fileName, boolean fileExists) throws IOException {
		super(fileName, fileExists);
	}

	/**
	 * Will return a zip parameter object, with custom values fitting the situation.
	 * 
	 * @return a ZipParameters object
	 */
	private static ZipParameters setupZipParameters() {
		return new ZipParameters();
	}

	/**
	 * Will remove the respective file header from the zip file.
	 * 
	 * @param fileHeader the file to remove
	 * @throws IOException if something unexpected happened
	 */
	private void removeFileHeader(FileHeader fileHeader) throws IOException {
		zipFile.removeFile(fileHeader);
	}

	/**
	 * Will add a stream of bytes to the folder.
	 *
	 * @param fileContent the input stream to write to the archive
	 * @throws ZipException if something unexpected happend
	 */
	private void addStream(InputStream fileContent, ZipParameters params) throws IOException {
		zipFile.addStream(fileContent, params);
	}

	@Override
	public void addFolder(String path) throws IOException {
		if (!path.endsWith(ARCHIVE_DELIMITER)) {
			throw new IOException(String.format("%s is not a valid directory path", path));
		}
		ZipParameters params = setupZipParameters();
		String zipPath = String.format("%s%s", pathToZipPath(path), ARCHIVE_DELIMITER);

		if (!hasParentFolder(zipPath))
			throw new IOException("Parent folder doesn't exist");

		params.setFileNameInZip(zipPath);
		addStream(new ByteArrayInputStream(new byte[0]), params);

	}

	@Override
	public void addFile(String data, String name) throws IOException {
		InputStream is = new ByteArrayInputStream(data.getBytes());
		addFile(is, name);
	}

	@Override
	public void addFile(InputStream data, String filepath) throws IOException {
		ZipParameters params = setupZipParameters();
		// check if path may belong to file
		if (filepath.endsWith(ARCHIVE_DELIMITER)) {
			throw new IOException(String.format("%s is not a valid file path", filepath));
		}

		String zipPath = pathToZipPath(filepath);
		// check if parent exists in folder
		if (!hasParentFolder(zipPath))
			throw new IOException("Parent folder doesn't exist");

		params.setFileNameInZip(zipPath);
		addStream(data, params);
	}

	@Override
	public void removeFile(String path) throws IOException {
		if (!hasFile(path)) {
			throw new IOException(String.format("requested file <%s> doesn't exist", path));
		}

		FileHeader fileHeader = getFileHeader(path);

		if (fileHeader.isDirectory()) {
			throw new IOException("The file requested to be removed is a directory.");
		}
		removeFileHeader(fileHeader);

	}

	@Override
	public void removeFolder(String path) throws IOException {
		if (!path.endsWith(ARCHIVE_DELIMITER)) {
			throw new IOException("path is not a valid directory path.");
		}

		String zipPath = pathToZipPath(path) + ARCHIVE_DELIMITER;

		if (!hasFolder(zipPath)) {
			throw new IOException(String.format("requested folder <%s> doesn't exist", path));
		}
		FileHeader fileHeader = getFileHeader(path);
		if (!fileHeader.isDirectory()) {
			throw new IOException("The path requested to be removed is a file.");
		}
		removeFileHeader(fileHeader);
	}
}