package ch.zhaw.pm3.teamretro.ui;

import ch.zhaw.pm3.teamretro.ui.game.view.SplashScreenView;

/**
 * Entry point and host of main method of Jump and Build.
 * <p>
 * Instances a new View, and with it, launches the UI.
 */
public class JumpAndBuild {

    /**
     * Entry point of the application. Launches a splash screen.
     *
     * @param args Arguments passed by the operating system
     */
    public static void main(String[] args) {
        SplashScreenView view = new SplashScreenView();
        view.startUI();
    }
}
