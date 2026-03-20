package com.lollypop;

import com.lollypop.ui.MainWindow;
import com.lollypop.util.CrashRecoveryManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * JavaFX entry point for the Lollypop / Matsecom Subscriber Management System.
 * Handles crash-recovery lifecycle:
 *  1. On start: check for a leftover safety file → restore DB if found.
 *  2. Write safety file immediately after DB is verified OK.
 *  3. On clean exit: delete the safety file.
 */
public class LollypopApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Step 1 – crash recovery check
        CrashRecoveryManager recovery = new CrashRecoveryManager();
        recovery.checkAndRecover();

        // Step 2 – write safety file (app is now running)
        recovery.writeSafetyFile();

        // Step 3 – register shutdown hook to delete safety file on clean exit
        Runtime.getRuntime().addShutdownHook(new Thread(recovery::deleteSafetyFile));
        Platform.setImplicitExit(true);

        // Launch main window
        MainWindow mainWindow = new MainWindow(primaryStage, recovery);
        mainWindow.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
