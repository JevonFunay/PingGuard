package com.pingguard.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing the result of a single ping operation.
 * Contains the latency value, timestamp, and status information.
 */
public class PingStat {

    /** Constant indicating a Request Timed Out or error */
    public static final int RTO = -1;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String targetIp;
    private final int latencyMs;
    private final LocalDateTime timestamp;
    private final boolean isTimeout;

    /**
     * Creates a new PingStat.
     *
     * @param targetIp  the IP address that was pinged
     * @param latencyMs the measured latency in milliseconds, or {@link #RTO} for timeout/error
     */
    public PingStat(String targetIp, int latencyMs) {
        this.targetIp = targetIp;
        this.latencyMs = latencyMs;
        this.timestamp = LocalDateTime.now();
        this.isTimeout = (latencyMs == RTO);
    }


    public String getTargetIp() {
        return targetIp;
    }

    public int getLatencyMs() {
        return latencyMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isTimeout() {
        return isTimeout;
    }

    /**
     * Returns the formatted timestamp string (HH:mm:ss).
     */
    public String getFormattedTime() {
        return timestamp.format(FORMATTER);
    }

    /**
     * Returns a human-readable status string.
     */
    public String getStatusText() {
        if (isTimeout) {
            return "RTO / Unreachable";
        }
        return latencyMs + " ms";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s → %s",
                getFormattedTime(), targetIp, getStatusText());
    }
}
