package ch.zhaw.pm3.teamretro.ui.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Region;

/**
 * Abstraction layer for creating multiple different interaction windows with
 * the player.
 */
public class InteractionWindow {

    /**
     * Private constructor to hide the implicit one. (Recommended by SonarLint)
     */
    private InteractionWindow() {
    }

    /**
     * Spawns a new choice window.
     *
     * @param list        List of objects to choose from.
     * @param headerText  String for the header text.
     * @param messageText The message for the player.
     */
    public static Optional<Integer> choose(List<String> list, String headerText, String messageText) {
        Map<String, Integer> stringMap = new HashMap<>(list.size());
        for (int index = 0; index < list.size(); index += 1) {
            stringMap.put(list.get(index), index);
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(list.get(0), list);
        setupWindow(dialog, "Please choose...", headerText, messageText);

        Optional<String> result = dialog.showAndWait();
        Optional<Integer> choice = Optional.empty();

        if (result.isPresent()) {
            choice = Optional.of(stringMap.get(result.get()));
        }
        return choice;
    }

    /**
     * Spawns a new data entry window.
     *
     * @param headerText  String for the header text.
     * @param messageText The message for the player.
     */
    public static Optional<String> enter(String headerText, String messageText) {
        TextInputDialog dialog = new TextInputDialog("Enter text");
        setupWindow(dialog, "Data entry", headerText, messageText);
        return dialog.showAndWait();
    }

    /**
     * Spawns a new informational window.
     *
     * @param headerText  Text in the header.
     * @param messageText The message for the player.
     */
    public static void inform(String headerText, String messageText) {
        Alert alert = createAlert(Alert.AlertType.INFORMATION, "Information", headerText, messageText);
        alert.showAndWait();
    }

    /**
     * Spawns a new question window. Needs a header text and a message text.
     *
     * @param headerText  String for the header text.
     * @param messageText String for the message text.
     */
    public static boolean ask(String headerText, String messageText) {
        Alert alert = createAlert(Alert.AlertType.CONFIRMATION, "Question", headerText, messageText);

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);

        return alert.showAndWait().filter(buttonType -> buttonType == yesButton).isPresent();
    }

    /**
     * Creates an alert window.
     *
     * @param alertType   Type of alert.
     * @param titleText   Text in the window title.
     * @param headerText  Text in the header.
     * @param messageText The message for the player.
     * @return a new Alert instance.
     */
    private static Alert createAlert(Alert.AlertType alertType, String titleText, String headerText,
            String messageText) {
        Alert alert = new Alert(alertType);
        setupWindow(alert, titleText, headerText, messageText);
        return alert;
    }

    /**
     * Creates a dialog window and formats it properly.
     *
     * @param dialog      Type of dialog window.
     * @param titleText   Text in the window title.
     * @param headerText  Text in the header.
     * @param messageText The message for the player.
     * @param <T>         Generic type for different types of Dialog instances.
     */
    private static <T> void setupWindow(Dialog<T> dialog, String titleText, String headerText, String messageText) {
        dialog.setResizable(false);
        dialog.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        dialog.setTitle(titleText);
        dialog.setHeaderText(headerText);
        dialog.setContentText(messageText);
    }
}
