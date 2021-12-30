package ch.zhaw.pm3.teamretro.logic.game;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.AnimationTimer;

/**
 * <p>
 * Define a custom timed loop for our inner game loop and timing.
 * </p>
 *
 */
public abstract class TimedLoop extends AnimationTimer {
    /**
     * Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger(TimedLoop.class.getName());

    /**
     * How many nanoseconds are in a second
     */
    private static final int NANOSECONDS_IN_SECOND = 1_000_000_000;

    /**
     * Multiplier to convert nanoseconds to milliseconds and vice-versa.
     */
    private static final double NANOSECONDS_TO_MILLISECONDS_MULTIPLIER = 1E3;

    /**
     * How much time [nanoseconds] has passed since the last frame
     */
    protected long timeSinceLastFrame;
    /**
     * Keeps track of the amount of frames generated in the last second.
     */
    protected long frameCount;

    /**
     * How long the last frame took to generate [nanoseconds].
     */
    protected double lastFrameTime;

    /**
     * Target frames per seconds [FPS] the game should run in.
     */
    private static final long DESIRED_FPS = 120;

    /**
     * Amount of time the loop should pause between frames to hit the target FPS.
     */
    protected static final long FRAME_SLEEP_TIME_MS = (long) (NANOSECONDS_TO_MILLISECONDS_MULTIPLIER / DESIRED_FPS);

    protected TimedLoop() {
        lastFrameTime = 0;
        timeSinceLastFrame = 0;
        frameCount = 0;
    }

    /**
     * Abstract method for implementing logic specific to the game (the inner game
     * loop).
     * 
     * @param lastFrameTime The time it took to generate the last game loop cycle in
     *                      seconds [s].
     */
    public abstract void runInnerLoop(double lastFrameTime, boolean secondPass);

    /**
     * @param frameTime Timestamp in nanoseconds
     */
    @Override
    public synchronized void handle(long frameTime) {
        boolean secHappend = false; // is true when a second passed

        lastFrameTime = frameTime - lastFrameTime;

        lastFrameTime /= NANOSECONDS_IN_SECOND; // makes lastFrameTime unit into "seconds".

        if (timeSinceLastFrame == 0) {
            timeSinceLastFrame = frameTime;
        } else if ((frameTime - timeSinceLastFrame) >= NANOSECONDS_IN_SECOND) {
            String fps = String.format("framerate: %dfps%n", frameCount);
            String logFrameTime = String.format("frametime: %fms%n",
                    lastFrameTime * NANOSECONDS_TO_MILLISECONDS_MULTIPLIER);
            LOGGER.log(Level.INFO, fps);
            LOGGER.log(Level.FINE, logFrameTime);
            frameCount = 0;
            secHappend = true;
            timeSinceLastFrame = frameTime;
        }

        runInnerLoop(lastFrameTime, secHappend);

        ++frameCount;
        lastFrameTime = frameTime;

        try {
            // no loop is required here (as suggested by the lint),
            // because it is not waited for a specific condition
            // except waiting until the next iteration begins.
            this.wait(FRAME_SLEEP_TIME_MS, 0);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Game loop frame limiter interrupted. Stopping.");
            stop();
            Thread.currentThread().interrupt();
        }
    }

}
