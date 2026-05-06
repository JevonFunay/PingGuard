package com.pingguard;

import com.pingguard.controller.MainController;
import com.pingguard.util.SystemTrayUtil;
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
        Platform.setImplicitExit(false);

        trayUtil = new SystemTrayUtil();
        trayUtil.setupTray();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainWindow.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setTrayUtil(trayUtil);

        primaryStage.setTitle("PingGuard - Network Monitor");
        primaryStage.setResizable(false);

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

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            primaryStage.hide();
            trayUtil.showNotification(
                "PingGuard",
                "Aplikasi berjalan di background. Klik dua kali ikon tray untuk membuka kembali.",
                TrayIcon.MessageType.INFO
            );
        });

        trayUtil.setOnTrayDoubleClick(() -> Platform.runLater(primaryStage::show));
    }

    @Override
    public void stop() {
        if (trayUtil != null) {
            trayUtil.removeTray();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
