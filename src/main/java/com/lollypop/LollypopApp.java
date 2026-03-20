package com.lollypop;

import com.lollypop.dao.DatabaseConnection;
import com.lollypop.ui.DatabaseConfigDialog;
import com.lollypop.ui.MainWindow;
import com.lollypop.util.CrashRecoveryManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * JavaFX entry point for the Lollypop / Matsecom Subscriber Management System.
 * Handles database setup and crash-recovery lifecycle.
 */
public class LollypopApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Optional<DatabaseConnection.DbConfig> config = new DatabaseConfigDialog(
                primaryStage,
                DatabaseConnection.getCurrentConfig()
        ).showAndWait();

        if (config.isEmpty()) {
            Platform.exit();
            return;
        }

        CrashRecoveryManager recovery = new CrashRecoveryManager();
        recovery.checkAndRecover();
        recovery.writeSafetyFile();

        Runtime.getRuntime().addShutdownHook(new Thread(recovery::deleteSafetyFile));
        Platform.setImplicitExit(true);

        MainWindow mainWindow = new MainWindow(primaryStage, recovery);
        mainWindow.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
