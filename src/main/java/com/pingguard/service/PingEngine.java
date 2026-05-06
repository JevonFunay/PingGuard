package com.pingguard.service;

import com.pingguard.model.PingStat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class responsible for executing a native Windows ping command
 * and parsing the output to extract latency information.
 *
 * Uses ProcessBuilder instead of InetAddress.isReachable() because:
 * 1. InetAddress.isReachable() is often blocked by firewalls.
 * 2. It does not return latency values on Windows.
 */
public class PingEngine {

    /**
     * Regex pattern to match latency values in ping output.
     * Handles both English ("time=XXms" or "time<1ms") and
     * Indonesian locale ("waktu=XXms" or "waktu<1ms").
     */
    private static final Pattern LATENCY_PATTERN =
            Pattern.compile("(?:time|waktu)[=<](\\d+)\\s*ms", Pattern.CASE_INSENSITIVE);

    /**
     * Executes a single ping to the specified IP address and returns the result.
     *
     * @param ipAddress the target IP address or hostname to ping
     * @return a {@link PingStat} containing the latency or RTO indicator
     */
    public PingStat ping(String ipAddress) {
        try {
            ProcessBuilder pb = new ProcessBuilder("ping", "-n", "1", "-w", "3000", ipAddress);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.contains("Request timed out")
                        || line.contains("Destination host unreachable")
                        || line.contains("Habis waktu")
                        || line.contains("could not find host")
                        || line.contains("Ping request could not find host")) {
                    process.waitFor();
                    return new PingStat(ipAddress, PingStat.RTO);
                }

                Matcher matcher = LATENCY_PATTERN.matcher(line);
                if (matcher.find()) {
                    int latency = Integer.parseInt(matcher.group(1));
                    process.waitFor();
                    return new PingStat(ipAddress, latency);
                }
            }

            process.waitFor();
        } catch (Exception e) {
            System.err.println("PingEngine error: " + e.getMessage());
        }

        return new PingStat(ipAddress, PingStat.RTO);
    }

    /**
     * Returns the raw latency value in milliseconds, or -1 on failure.
     * Convenience method for simple checks.
     *
     * @param ipAddress the target IP address
     * @return latency in ms, or -1
     */
    public int getPingLatency(String ipAddress) {
        return ping(ipAddress).getLatencyMs();
    }
}
