package ch.zhaw.pm3.teamretro.logic.common;

import java.util.EnumSet;
import java.util.Set;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

/**
 * Key event handler class to receive events caused by the keyboard. It's cool.
 * And nice.
 */
public class KeyEventHandler {

    private final Set<KeyCode> currentlyActiveKeys = EnumSet.noneOf(KeyCode.class);

    /**
     * creates the handler
     */
    public KeyEventHandler() {
        // there is no setup needed here
    }

    /**
     * Registers key handler to given node.
     *
     * @param scene Scene of node to register the key events to.
     */
    public void setupActionHandler(Scene scene) {
        scene.setOnKeyPressed(event -> currentlyActiveKeys.add(event.getCode()));
        scene.setOnKeyReleased(event -> currentlyActiveKeys.remove(event.getCode()));
    }

    /**
     * Will return the currently active keys.
     * 
     * @return the keys
     */
    public Set<KeyCode> getCurrentlyActiveKeys() {
        return currentlyActiveKeys;
    }
}
