package com.pingguard.controller;
import com.pingguard.model.AppConfig;
import com.pingguard.model.PingStat;
import com.pingguard.service.BackgroundTask;
import com.pingguard.util.ConfigManager;
import com.pingguard.util.SystemTrayUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.awt.TrayIcon;
import java.net.URL;

import java.util.ResourceBundle;

/**
 * Controller for the main application window.
 * Handles user input validation, start/stop monitoring, and UI updates.
 */
public class MainController implements Initializable {

    @FXML private Label statusLabel;
    @FXML private Label statusIndicator;
    @FXML private Label latencyLabel;
    @FXML private Label lastPingLabel;
    @FXML private Label pingCountLabel;
    @FXML private Label spikeCountLabel;
    @FXML private Label rtoCountLabel;
    @FXML private TextField ipField;
    @FXML private TextField thresholdField;
    @FXML private TextField intervalField;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Button exitButton;
    @FXML private TextArea logArea;
    @FXML private VBox settingsPane;

    private SystemTrayUtil trayUtil;

    private BackgroundTask backgroundTask;
    private AppConfig currentConfig;
    private int totalPings = 0;
    private int spikeCount = 0;
    private int rtoCount = 0;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentConfig = ConfigManager.load();

        ipField.setText(currentConfig.getTargetIp());
        thresholdField.setText(String.valueOf(currentConfig.getThresholdMs()));
        intervalField.setText(String.valueOf(currentConfig.getIntervalSeconds()));

        stopButton.setDisable(true);
        logArea.setEditable(false);
        logArea.setWrapText(true);

        updateStatusUI(false, null);
        appendLog("PingGuard v1.0 ready. Configure settings and press Start.");
    }

    /**
     * Injects the SystemTrayUtil dependency from App.java.
     */
    public void setTrayUtil(SystemTrayUtil trayUtil) {
        this.trayUtil = trayUtil;
    }


    /**
     * Handles the "Start Monitoring" button click.
     */
    @FXML
    private void onStartMonitoring() {
        String ip = ipField.getText().trim();
        if (ip.isEmpty()) {
            showAlert("Validation Error", "Target IP cannot be empty.");
            return;
        }

        int threshold;
        try {
            threshold = Integer.parseInt(thresholdField.getText().trim());
            if (threshold <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Threshold must be a positive integer (ms).");
            return;
        }

        int interval;
        try {
            interval = Integer.parseInt(intervalField.getText().trim());
            if (interval <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Interval must be a positive integer (seconds).");
            return;
        }

        currentConfig = new AppConfig(ip, threshold, interval);
        ConfigManager.save(currentConfig);

        totalPings = 0;
        spikeCount = 0;
        rtoCount = 0;

        startButton.setDisable(true);
        stopButton.setDisable(false);
        setFieldsEditable(false);
        updateStatusUI(true, null);

        appendLog("▶ Started monitoring " + ip
                + " | Threshold: " + threshold + "ms"
                + " | Interval: " + interval + "s");

        backgroundTask = new BackgroundTask();
        backgroundTask.start(currentConfig, this::onPingResult);
    }

    /**
     * Handles the "Stop Monitoring" button click.
     */
    @FXML
    private void onStopMonitoring() {
        if (backgroundTask != null) {
            backgroundTask.stop();
            backgroundTask = null;
        }

        startButton.setDisable(false);
        stopButton.setDisable(true);
        setFieldsEditable(true);
        updateStatusUI(false, null);

        appendLog("⏹ Monitoring stopped.");
    }

    /**
     * Handles the "Exit Application" button click.
     * This is the ONLY way to fully terminate the application.
     */
    @FXML
    private void onExitApplication() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Exit PingGuard");
        confirm.setHeaderText("Are you sure?");
        confirm.setContentText("This will completely stop PingGuard, including background monitoring.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (backgroundTask != null) {
                    backgroundTask.stop();
                }
                if (trayUtil != null) {
                    trayUtil.removeTray();
                }
                Platform.exit();
                System.exit(0);
            }
        });
    }


    /**
     * Called from the background thread each time a ping completes.
     * Must use Platform.runLater() for UI updates.
     */
    private void onPingResult(PingStat stat) {
        Platform.runLater(() -> {
            totalPings++;

            if (stat.isTimeout()) {
                rtoCount++;
                updateStatusUI(true, stat);
                appendLog("❌ [" + stat.getFormattedTime() + "] "
                        + stat.getTargetIp() + " → RTO / Unreachable");

                if (trayUtil != null) {
                    trayUtil.showNotification(
                            "⚠ Connection Lost!",
                            "Ping to " + stat.getTargetIp() + " timed out (Request Timed Out).",
                            TrayIcon.MessageType.ERROR
                    );
                }
            } else if (stat.getLatencyMs() > currentConfig.getThresholdMs()) {
                spikeCount++;
                updateStatusUI(true, stat);
                appendLog("⚠ [" + stat.getFormattedTime() + "] "
                        + stat.getTargetIp() + " → " + stat.getLatencyMs() + "ms"
                        + " (SPIKE! Threshold: " + currentConfig.getThresholdMs() + "ms)");

                if (trayUtil != null) {
                    trayUtil.showNotification(
                            "🔺 Ping Spike Detected!",
                            "Latency to " + stat.getTargetIp() + " is "
                                    + stat.getLatencyMs() + "ms (threshold: "
                                    + currentConfig.getThresholdMs() + "ms).",
                            TrayIcon.MessageType.WARNING
                    );
                }
            } else {
                updateStatusUI(true, stat);
                appendLog("✅ [" + stat.getFormattedTime() + "] "
                        + stat.getTargetIp() + " → " + stat.getLatencyMs() + "ms");
            }

            pingCountLabel.setText(String.valueOf(totalPings));
            spikeCountLabel.setText(String.valueOf(spikeCount));
            rtoCountLabel.setText(String.valueOf(rtoCount));
        });
    }


    /**
     * Updates the status indicator and latency display.
     */
    private void updateStatusUI(boolean isRunning, PingStat stat) {
        if (!isRunning) {
            statusIndicator.setText("⏸");
            statusIndicator.setStyle("-fx-text-fill: #888888;");
            statusLabel.setText("Idle");
            statusLabel.setStyle("-fx-text-fill: #888888;");
            latencyLabel.setText("-- ms");
            latencyLabel.setStyle("-fx-text-fill: #aaaaaa;");
            lastPingLabel.setText("--:--:--");
            return;
        }

        if (stat == null) {
            statusIndicator.setText("🟢");
            statusLabel.setText("Running");
            statusLabel.setStyle("-fx-text-fill: #00e676;");
            return;
        }

        lastPingLabel.setText(stat.getFormattedTime());

        if (stat.isTimeout()) {
            statusIndicator.setText("🔴");
            statusLabel.setText("RTO");
            statusLabel.setStyle("-fx-text-fill: #ff1744;");
            latencyLabel.setText("TIMEOUT");
            latencyLabel.setStyle("-fx-text-fill: #ff1744;");
        } else if (stat.getLatencyMs() > currentConfig.getThresholdMs()) {
            statusIndicator.setText("🟡");
            statusLabel.setText("Spike");
            statusLabel.setStyle("-fx-text-fill: #ffab00;");
            latencyLabel.setText(stat.getLatencyMs() + " ms");
            latencyLabel.setStyle("-fx-text-fill: #ffab00;");
        } else {
            statusIndicator.setText("🟢");
            statusLabel.setText("Normal");
            statusLabel.setStyle("-fx-text-fill: #00e676;");
            latencyLabel.setText(stat.getLatencyMs() + " ms");
            latencyLabel.setStyle("-fx-text-fill: #00e676;");
        }
    }

    private static final int MAX_LOG_LINES = 200;

    /**
     * Appends a log entry to the log TextArea and auto-scrolls.
     * Trims old entries when exceeding MAX_LOG_LINES to keep memory bounded.
     */
    private void appendLog(String message) {
        logArea.appendText(message + "\n");

        String text = logArea.getText();
        int lineCount = 0;
        int idx = text.length();
        while (idx > 0 && lineCount < MAX_LOG_LINES) {
            idx = text.lastIndexOf('\n', idx - 1);
            lineCount++;
        }
        if (lineCount >= MAX_LOG_LINES && idx > 0) {
            logArea.setText(text.substring(idx + 1));
        }

        logArea.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * Enables or disables input fields during monitoring.
     */
    private void setFieldsEditable(boolean editable) {
        ipField.setEditable(editable);
        thresholdField.setEditable(editable);
        intervalField.setEditable(editable);

        double opacity = editable ? 1.0 : 0.6;
        ipField.setOpacity(opacity);
        thresholdField.setOpacity(opacity);
        intervalField.setOpacity(opacity);
    }

    /**
     * Shows a simple alert dialog.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
