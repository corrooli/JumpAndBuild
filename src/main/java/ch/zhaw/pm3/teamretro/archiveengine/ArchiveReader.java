package ch.zhaw.pm3.teamretro.archiveengine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * The main reader interface to be exposed to the upper systems.
 */
public interface ArchiveReader {

    /**
     * Will extract the file content as String. This method uses the UTF-8 charset
     * to decode.
     * 
     * @param filename the name of the file to extract
     * @return a String object
     * @throws IOException if the file doesn't exists or other
     */
    public String getText(String filename) throws IOException;

    /**
     * Will extract the file content as String.
     * 
     * @param filename the name of the file to extract
     * @param charset  the charset used to decode the string
     * @return a String object
     * @throws IOException if the file doesn't exists or other
     */
    public String getText(String filename, Charset charset) throws IOException;

    /**
     * This method will retrieve the given file as a raw InputStream.
     * 
     * @param filename the requested name of the file
     * @return a raw InputStream to extract the bytes from
     * @throws IOException if the file doesn't exist or other
     */
    public InputStream getFile(String filename) throws IOException;

    /**
     * Will check if the given file exists in the archive.
     * 
     * @param filename the file name
     * @return true if exist false otherwise
     * @throws IOException if something unknown happened
     */
    public boolean hasFile(String filename) throws IOException;

    /**
     * Will check if the given Folder exists in the archive.
     * 
     * @param dirname the file name
     * @return true if exist false otherwise
     * @throws IOException if something unknown happened
     */
    public boolean hasFolder(String dirname) throws IOException;

    /**
     * This method will return the archive name.
     */
    public String getName();

}
