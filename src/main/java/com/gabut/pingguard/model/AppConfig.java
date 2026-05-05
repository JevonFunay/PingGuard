package com.gabut.pingguard.model;

/**
 * Model class representing the user's monitoring configuration.
 * Holds the target IP address, latency threshold, and ping interval.
 */
public class AppConfig {

    private String targetIp;
    private int thresholdMs;
    private int intervalSeconds;

    /**
     * Creates a new AppConfig with default values.
     */
    public AppConfig() {
        this.targetIp = "8.8.8.8";
        this.thresholdMs = 100;
        this.intervalSeconds = 5;
    }

    /**
     * Creates a new AppConfig with the specified values.
     *
     * @param targetIp        the IP address to ping
     * @param thresholdMs     the maximum acceptable latency in milliseconds
     * @param intervalSeconds the interval between pings in seconds
     */
    public AppConfig(String targetIp, int thresholdMs, int intervalSeconds) {
        this.targetIp = targetIp;
        this.thresholdMs = thresholdMs;
        this.intervalSeconds = intervalSeconds;
    }

    // --- Getters & Setters ---

    public String getTargetIp() {
        return targetIp;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public int getThresholdMs() {
        return thresholdMs;
    }

    public void setThresholdMs(int thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    @Override
    public String toString() {
        return String.format("AppConfig{targetIp='%s', thresholdMs=%d, intervalSeconds=%d}",
                targetIp, thresholdMs, intervalSeconds);
    }
}
