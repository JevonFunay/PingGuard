package com.pingguard.util;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Utility class for managing the System Tray icon and
 * native Windows toast notifications.
 *
 * Uses java.awt.SystemTray and java.awt.TrayIcon to display
 * notifications without needing the JavaFX UI to be visible.
 */
public class SystemTrayUtil {

    private TrayIcon trayIcon;
    private Runnable onDoubleClick;

    /**
     * Initializes the system tray icon.
     * Must be called from the main thread or before any AWT interaction.
     */
    public void setupTray() {
        if (!SystemTray.isSupported()) {
            System.err.println("SystemTray is not supported on this platform.");
            return;
        }

        Image image = Toolkit.getDefaultToolkit()
                .getImage(getClass().getResource("/icon/app-icon.png"));

        trayIcon = new TrayIcon(image, "PingGuard - Network Monitor");
        trayIcon.setImageAutoSize(true);

        PopupMenu popup = new PopupMenu();

        MenuItem openItem = new MenuItem("Open PingGuard");
        openItem.addActionListener(e -> {
            if (onDoubleClick != null) {
                onDoubleClick.run();
            }
        });

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> {
            removeTray();
            System.exit(0);
        });

        popup.add(openItem);
        popup.addSeparator();
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && onDoubleClick != null) {
                    onDoubleClick.run();
                }
            }
        });

        try {
            SystemTray.getSystemTray().add(trayIcon);
        } catch (AWTException e) {
            System.err.println("Failed to add tray icon: " + e.getMessage());
        }
    }

    /**
     * Displays a native Windows toast notification.
     *
     * @param title   the notification title
     * @param message the notification body text
     * @param type    the message type (INFO, WARNING, ERROR, NONE)
     */
    public void showNotification(String title, String message, TrayIcon.MessageType type) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, type);
        }
    }

    /**
     * Removes the tray icon from the system tray.
     * Should be called during application shutdown.
     */
    public void removeTray() {
        if (trayIcon != null) {
            SystemTray.getSystemTray().remove(trayIcon);
            trayIcon = null;
        }
    }

    /**
     * Sets the callback to be invoked when the tray icon is double-clicked.
     *
     * @param callback a Runnable to execute on double-click
     */
    public void setOnTrayDoubleClick(Runnable callback) {
        this.onDoubleClick = callback;
    }
}
