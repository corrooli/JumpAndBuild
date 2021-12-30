package ch.zhaw.pm3.teamretro.ui.common;

import java.io.File;

import javafx.scene.Scene;
import javafx.stage.FileChooser;

/**
 * Abstraction layer for FileBrowser. Makes it easy and breezy to open files via
 * JavaFX fileChooser. Needs to be instantiated to receive information about the
 * chosen file.
 */
public class FileBrowser {

    /**
     * The file chosen by the user.
     */
    private File file;

    /**
     * Parent scene of the FileBrowser
     */
    private final Scene scene;

    /**
     * Constructor of the FileBrowser
     *
     * @param scene Parent scene instance of calling controller.
     */
    public FileBrowser(Scene scene) {
        this.scene = scene;
    }

    /**
     * Opening files.
     */
    public boolean openFile(String requiredFileType) {
        FileChooser fileChooser = fileChooserHelper(requiredFileType);
        file = fileChooser.showOpenDialog(scene.getWindow());
        return file != null;
    }

    /**
     * Saving files.
     */
    public boolean saveFile(String requiredFileType) {
        FileChooser fileChooser = fileChooserHelper(requiredFileType);
        file = fileChooser.showSaveDialog(scene.getWindow());
        return file != null;
    }

    /**
     * Helper method for creating a FileChooser and applying the filter of the
     * desired file extension.
     *
     * @param requiredFileType String of the desired file extension, e.g. "zip" for
     *                         zip file, "png" for PNG graphics...
     * @return FileChooser Instance, ready to open or save files
     */
    public FileChooser fileChooserHelper(String requiredFileType) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                requiredFileType + " files (*." + requiredFileType + ")", "*." + requiredFileType);
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser;
    }

    /**
     * Passes back the absolute path of the chosen file.
     *
     * @return String of the absolute path
     */
    public String getFileName() {
        return file.getAbsolutePath();
    }

    public File getFile() {
        return file;
    }
}
