package ch.zhaw.pm3.teamretro.logic.common;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.entity.Entity;
import ch.zhaw.pm3.teamretro.gamepack.entity.Position;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Abstraction layer to control the canvas, which displays the image of the
 * game. Responsible for drawing images and clearing the screen.
 */
public class CanvasController {
    /**
     * Main canvas instance.
     */
    private final Canvas canvas;

    /**
     * JavaFX GraphicsContext, needed for drawing images.
     */
    private final GraphicsContext graphicsContext;

    /**
     * Width of view port (canvas)
     */
    private final double width;

    /**
     * Height of view port (canvas)
     */
    private final double height;

    /**
     * Camera offset for all things drawn on canvas. y-axis not used for scrolling,
     * but leaves the possibility open.
     */
    private final Position camOffset;

    /**
     * Constructor of the CanvasController. Needs a root canvas instance passed by
     * the UI Controller class. Sets up the camera
     *
     * @param canvas Canvas, instantiated by the main UI controller.
     */
    public CanvasController(Canvas canvas) {
        this.canvas = canvas;
        graphicsContext = canvas.getGraphicsContext2D();
        width = canvas.getWidth();
        height = canvas.getHeight();
        camOffset = new Position(0, 0);
    }

    /**
     * Draws an entity on the canvas.
     *
     * @param entity Entity instance that is drawn on the canvas.
     */
    public void drawImage(Entity entity) throws InvalidLevelConfiguration {
        Image image = entity.getCurrentImage();
        double flipFactor = entity.isFlipped() ? -1 : 1;
        double offset = entity.isFlipped() ? image.getWidth() : 0;
        offset += camOffset.getX();
        graphicsContext.drawImage(image, entity.getPosition().getX() + offset, entity.getPosition().getY(),
                image.getWidth() * flipFactor, image.getHeight());
    }

    /**
     * Clears the image and redraws the background image.
     */
    public void clearImage(Image bg) {
        graphicsContext.clearRect(0, 0, width, height);
        graphicsContext.drawImage(bg, 0, 0);
    }

    /**
     * Set the camera offset.
     *
     * @param x x axis (pixels)
     * @param y y axis (pixels)
     */
    public void setCamOffset(double x, double y) {
        camOffset.setX(x);
        camOffset.setY(y);
    }

    /**
     * Helper method to move the camera by a given amount.
     *
     * @param delta deviation (pixels)
     */
    public void moveCamera(double delta) {
        camOffset.setX(camOffset.getX() - delta);
    }

    public Position getCamOffset() {
        return camOffset;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}