package ch.zhaw.pm3.teamretro.ui.common;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.logging.Logger;

/**
 * Helper class to handle all messaging to the user. Also used as last instance
 * of exceptions. All messages are logged with a logger.
 */
public class MessageHandler {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger(MessageHandler.class.getName());

    /**
     * Private constructor to hide the implicit one. (Recommended by SonarLint)
     */
    private MessageHandler() {
    }

    /**
     * Gives an error message in case something goes wrong.
     *
     * @param exception The exception that was thrown.
     */
    public static void handleException(Exception exception) {
        InteractionWindow.inform("Error!", exception.getMessage());
        LOGGER.log(SEVERE, exception.getMessage());
    }

    /**
     * Helper method to create warning windows.
     *
     * @param message Message to the user.
     */
    public static void createWarningWindow(String message) {
        InteractionWindow.inform("Warning!", message);
        LOGGER.log(FINE, message);
    }

    /**
     * Helper method to create error windows.
     *
     * @param message Message to the user.
     */
    public static void createErrorWindow(String message) {
        InteractionWindow.inform("Error!", message);
        LOGGER.log(WARNING, message);
    }

    /**
     * Helper method to create save success windows.
     *
     * @param message Message to the user.
     */
    public static void createSavedWindow(String message) {
        InteractionWindow.inform("Save successful!", message);
        LOGGER.log(INFO, message);
    }

    /**
     * Gives a warning in case the user tries to modify the level that's being play
     * tested right now.
     */
    public static void createPlayTestWarningWindow() {
        InteractionWindow.inform("Warning!", "You can't edit while play testing!");
        LOGGER.log(FINER, "User tried to edit the level while play testing.");
    }
}
