package com.pingguard.service;

import com.pingguard.model.AppConfig;
import com.pingguard.model.PingStat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manages a background thread that periodically pings the target IP
 * using a ScheduledExecutorService.
 *
 * This class decouples the ping scheduling from the UI layer,
 * communicating results via a callback (Consumer).
 */
public class BackgroundTask {

    private ScheduledExecutorService scheduler;
    private final PingEngine pingEngine;
    private volatile boolean running;

    public BackgroundTask() {
        this.pingEngine = new PingEngine();
        this.running = false;
    }

    /**
     * Starts the periodic ping monitoring.
     *
     * @param config   the application configuration (IP, threshold, interval)
     * @param callback a callback invoked on each ping result (called from background thread)
     */
    public void start(AppConfig config, Consumer<PingStat> callback) {
        if (running) {
            System.out.println("BackgroundTask: Already running.");
            return;
        }

        running = true;
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "PingGuard-Worker");
            t.setDaemon(true);
            return t;
        });

        Runnable pingTask = () -> {
            try {
                PingStat stat = pingEngine.ping(config.getTargetIp());
                callback.accept(stat);
            } catch (Exception e) {
                System.err.println("BackgroundTask error: " + e.getMessage());
            }
        };

        scheduler.scheduleWithFixedDelay(
                pingTask,
                0,
                config.getIntervalSeconds(),
                TimeUnit.SECONDS
        );

        System.out.println("BackgroundTask: Started monitoring " + config.getTargetIp()
                + " every " + config.getIntervalSeconds() + "s");
    }

    /**
     * Stops the periodic ping monitoring gracefully.
     */
    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            try {
                scheduler.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        running = false;
        System.out.println("BackgroundTask: Stopped.");
    }

    /**
     * Returns whether the background task is currently running.
     */
    public boolean isRunning() {
        return running;
    }
}
