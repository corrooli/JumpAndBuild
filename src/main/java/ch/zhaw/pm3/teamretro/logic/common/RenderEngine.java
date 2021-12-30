package ch.zhaw.pm3.teamretro.logic.common;

import ch.zhaw.pm3.teamretro.gamepack.InvalidLevelConfiguration;
import ch.zhaw.pm3.teamretro.gamepack.Level;
import ch.zhaw.pm3.teamretro.gamepack.entity.Block;
import ch.zhaw.pm3.teamretro.gamepack.entity.Enemy;
import ch.zhaw.pm3.teamretro.gamepack.entity.Item;
import ch.zhaw.pm3.teamretro.gamepack.entity.Player;
import ch.zhaw.pm3.teamretro.gamepack.sprite.Animation;

/**
 * Abstraction layer to draw all entities on screen.
 */
public class RenderEngine {
    /**
     * Reference to current level.
     */
    private Level currentLevel;

    /**
     * Canvas controller, helps drawing objects on the screen.
     */
    private CanvasController canvasController;

    /**
     * Renders all entities on screen.
     *
     * @throws InvalidLevelConfiguration In case an illegal level configuration was
     *                                   encountered while drawing.
     */
    public void render() throws InvalidLevelConfiguration {
        canvasController.clearImage(currentLevel.getBackground().getImages(Animation.IDLE).get(0));

        for (Block blk : currentLevel.getBlockList().values()) {
            canvasController.drawImage(blk);
        }

        for (Enemy enmy : currentLevel.getEnemyList().values()) {
            canvasController.drawImage(enmy);
        }

        Player p = currentLevel.getPlayer();
        canvasController.drawImage(p);

        for (Item item : currentLevel.getItemList().values()) {
            canvasController.drawImage(item);
        }
    }

    public void setCurrentLevel(Level currentLevel) {
        this.currentLevel = currentLevel;
    }

    public void setCanvasController(CanvasController canvasController) {
        this.canvasController = canvasController;
    }
}
