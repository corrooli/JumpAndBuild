package ch.zhaw.pm3.teamretro.gamepack;

/**
 * A custom exception with the purpose of representing any type of invalid level
 * configurations. Be is from an invalid defined level json or a wrongly deleted
 * entity this exception handles them all.
 */
public class InvalidLevelConfiguration extends Exception {

    /**
     * generated serial id used for hash generation
     */
    private static final long serialVersionUID = 1L;

    public InvalidLevelConfiguration(String message) {
        super(message);
    }
}
