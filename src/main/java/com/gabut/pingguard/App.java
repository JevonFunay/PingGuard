package com.gabut.pingguard;

import com.gabut.pingguard.controller.MainController;
import com.gabut.pingguard.util.SystemTrayUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.TrayIcon;
import java.util.Objects;

/**
 * PingGuard Application Entry Point.
 *
 * Handles JavaFX lifecycle, System Tray integration, and
 * ensures the app persists in background when the window is closed.
 */
public class App extends Application {

    private SystemTrayUtil trayUtil;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Prevent JavaFX from shutting down when all windows are hidden
        Platform.setImplicitExit(false);

        // Initialize the System Tray utility
        trayUtil = new SystemTrayUtil();
        trayUtil.setupTray();

        // Load the FXML layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();

        // Pass dependencies to the controller
        MainController controller = loader.getController();
        controller.setTrayUtil(trayUtil);

        // Configure the stage
        primaryStage.setTitle("PingGuard - Network Monitor");
        primaryStage.setResizable(false);

        // Set application icon
        try {
            primaryStage.getIcons().add(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icon/app-icon.png")))
            );
        } catch (Exception e) {
            System.err.println("Could not load application icon: " + e.getMessage());
        }

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();

        // When user clicks (X), hide the window instead of closing the app
        primaryStage.setOnCloseRequest(event -> {
            event.consume(); // Prevent default close behavior
            primaryStage.hide();
            trayUtil.showNotification(
                "PingGuard",
                "Aplikasi berjalan di background. Klik dua kali ikon tray untuk membuka kembali.",
                TrayIcon.MessageType.INFO
            );
        });

        // Allow double-clicking on tray icon to re-show the window
        trayUtil.setOnTrayDoubleClick(() -> Platform.runLater(primaryStage::show));
    }

    @Override
    public void stop() {
        // Cleanup tray icon when application truly exits
        if (trayUtil != null) {
            trayUtil.removeTray();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
