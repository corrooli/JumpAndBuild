package ch.zhaw.pm3.teamretro.archiveengine;

import java.io.IOException;
import java.io.InputStream;

/**
 * The main writer interface to be exposed to the upper systems.
 */
public interface ArchiveWriter extends ArchiveReader {

    /**
     * Will add a folder to the created zip file, attention parent folders have to
     * already exist.
     * 
     * @param path the path to create
     * @throws IOException if something went wrong
     */
    public void addFolder(String path) throws IOException;

    /**
     * Will add the file with the String content to the given folder, attention the
     * folder has to already exist.
     * 
     * @param name the file name and path
     * @param data the data to save
     * @throws IOException if something went wrong
     */
    public void addFile(String data, String name) throws IOException;

    /**
     * Will add the file to the given folder, attention the folder has to already
     * exist.
     * 
     * @param data     the file name
     * @param filePath the directory path to add the file to
     * @throws IOException if something went wrong
     */
    public void addFile(InputStream data, String filePath) throws IOException;

    /**
     * Will remove a file from the given zip file
     * 
     * @param path the file path to remove
     * @throws IOException if the required file doesn't exist or other
     */
    public void removeFile(String path) throws IOException;

    /**
     * Will remove the folder with all the data it contains recursively
     * 
     * @param path the path from which to remove from
     * @throws IOException if the folder doesn't exist or other
     */
    public void removeFolder(String path) throws IOException;
}
