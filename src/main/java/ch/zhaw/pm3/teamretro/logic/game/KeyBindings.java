package ch.zhaw.pm3.teamretro.logic.game;

import javafx.scene.input.KeyCode;

/**
 * List of all available controls the player has.
 */
public enum KeyBindings {
    JUMP(new KeyCode[] { KeyCode.SPACE, KeyCode.W, KeyCode.J, KeyCode.UP }),
    WALK_LEFT(new KeyCode[] { KeyCode.A, KeyCode.LEFT }),
    WALK_RIGHT(new KeyCode[] { KeyCode.D, KeyCode.RIGHT }),
    RUN(new KeyCode[] { KeyCode.K, KeyCode.SHIFT }),
    RESET(new KeyCode[] { KeyCode.R });

    /**
     * Container for KeyCode Arrays.
     */
    private final KeyCode[] keyCodes;

    /**
     * Constructor of KeyBindings.
     * 
     * @param keyCodes Array of applicable KeyCodes.
     */
    KeyBindings(KeyCode[] keyCodes) {
        this.keyCodes = keyCodes;
    }

    /**
     * Returns a copy of the internal keycode lists.
     * @return the keycodes
     */
    public KeyCode[] getKeyCodes() {
        return keyCodes.clone();
    }
}