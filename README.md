# PingGuard

PingGuard is a lightweight, real-time network latency monitoring tool designed with native Windows notifications and background execution capabilities.

It continuously monitors the network connection by periodically sending ICMP echo requests to a specified target. If the latency exceeds a user-defined threshold, or if the connection times out, PingGuard alerts the user via a native Windows toast notification.

## Key Features

- Continuous Network Monitoring
  Monitors the connection by pinging any valid IP address or hostname at configurable intervals.

- Spike and Timeout Detection
  Automatically detects network latency spikes and Request Timed Out (RTO) errors, triggering alerts when the latency exceeds the maximum threshold.

- Native Windows Notifications
  Integrates seamlessly with the Windows operating system to display toast popups through the System Tray interface.

- Background Execution Mode
  Supports running continuously in the background. Closing the main application window minimizes the application to the system tray, allowing the monitoring loop to persist without consuming desktop space.

- Configuration Persistence
  Application settings and target configurations are automatically saved to the local file system (config.properties) and restored on startup.

- Minimal Resource Footprint
  Optimized for low memory consumption using targeted JVM tuning parameters and the Serial Garbage Collector.

## System Requirements

- Java Runtime Environment 21 or higher
- Microsoft Windows OS (Utilizes native Windows ICMP tools and System Tray APIs)

## Project Structure

The project is structured according to standard Maven conventions:

- pom.xml: Maven configuration and dependencies
- build.cmd: Script to compile and package the application into a JAR file
- build-exe.cmd: Script to bundle the application and Java Runtime into a standalone Windows Executable (.exe)
- run.cmd: Convenience script to execute the application locally
- src/main/java/com/pingguard/: Application source code and architecture

## Compilation and Execution

To run the application from the source code, execute the provided script:
> .\run.cmd

To build a standalone Windows Executable (.exe) that bundles the Java Runtime:
> .\build-exe.cmd

Once the executable build completes, the compiled application will be located in the `dist\PingGuard` directory. You can distribute this directory as a standalone application.

## Architecture and Design

PingGuard implements the Model-View-Controller (MVC) design pattern to ensure a clean separation between the graphical interface, application state, and core business logic.

- Network requests are delegated to the native operating system's ICMP implementation via ProcessBuilder to ensure accuracy and firewall compliance.
- Background execution and scheduling are managed by a daemonized ScheduledExecutorService, ensuring that polling intervals are strictly respected without blocking the main interface thread.
- SystemTray integration allows the JavaFX interface to be detached and re-attached dynamically.
