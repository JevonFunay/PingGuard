# 🛡️ PingGuard

**Real-time network latency monitor with native Windows notifications.**

PingGuard monitors your network connection by periodically pinging a target IP address. When latency exceeds your defined threshold or the connection drops, it sends a **native Windows toast notification** — even when minimized to the system tray.

![Java](https://img.shields.io/badge/Java-21%2B-orange?logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?logo=java)
![Platform](https://img.shields.io/badge/Platform-Windows-0078D6?logo=windows)
![License](https://img.shields.io/badge/License-MIT-green)

## ✨ Features

- **Live Monitoring** — Ping any IP/hostname at configurable intervals
- **Spike Detection** — Alerts when latency exceeds your threshold
- **Native Notifications** — Windows toast popups via `SystemTray` (no JavaFX UI needed)
- **Background Mode** — Closing the window hides to system tray, monitoring continues
- **Tray Integration** — Double-click tray icon to reopen, right-click for menu
- **Config Persistence** — Settings auto-saved to `~/.pingguard/config.properties`
- **Dual-Locale Ping Parser** — Handles both English (`time=`) and Indonesian (`waktu=`) Windows output
- **Memory Optimized** — Runs at ~100-130MB with SerialGC and capped heap

## 📋 Prerequisites

- **Java JDK 21+** (tested on JDK 25 Adoptium Temurin)
- **Windows OS** (uses native `ping.exe` and `SystemTray`)

## 🚀 Quick Start

```bash
# Clone the repository
git clone https://github.com/YOUR_USERNAME/PingGuard.git
cd PingGuard

# Run the application
.\run.cmd
```

> First run will download Maven dependencies (~50MB). Subsequent runs are instant.

## 🏗️ Project Structure

```
pingguard/
├── pom.xml                              # Maven build config
├── run.cmd                              # One-click run script
├── build.cmd                            # Build JAR script
└── src/main/
    ├── java/com/gabut/pingguard/
    │   ├── App.java                     # Entry point + tray lifecycle
    │   ├── controller/
    │   │   └── MainController.java      # UI events & ping callbacks
    │   ├── model/
    │   │   ├── AppConfig.java           # Configuration data
    │   │   └── PingStat.java            # Ping result data
    │   ├── service/
    │   │   ├── PingEngine.java          # Native CMD ping + regex parser
    │   │   └── BackgroundTask.java      # ScheduledExecutor ping loop
    │   └── util/
    │       ├── SystemTrayUtil.java       # Windows tray + notifications
    │       └── ConfigManager.java        # .properties persistence
    └── resources/
        ├── fxml/
        │   ├── MainWindow.fxml          # UI layout
        │   └── style.css                # Dark theme
        └── icon/
            └── app-icon.png             # App icon
```

## 🎯 How It Works

1. **Start** — Enter target IP, latency threshold (ms), and ping interval (s)
2. **Monitor** — Background thread pings using native `ping -n 1` via `ProcessBuilder`
3. **Detect** — Regex parses output for latency values
4. **Alert** — Native Windows toast notification on spike or timeout
5. **Background** — Close window → app hides to tray, monitoring continues
6. **Exit** — Only the "Exit Application" button fully terminates the app

## ⚙️ Configuration

Settings are auto-saved at `%USERPROFILE%\.pingguard\config.properties`:

| Setting | Default | Description |
|---------|---------|-------------|
| `target.ip` | `8.8.8.8` | IP address or hostname to ping |
| `threshold.ms` | `100` | Max acceptable latency (ms) |
| `interval.seconds` | `5` | Time between pings (seconds) |

## 🧠 Architecture

- **MVC Pattern** — Clean separation of Model, View (FXML), and Controller
- **ProcessBuilder** over `InetAddress.isReachable()` — Firewall-friendly, returns actual latency
- **ScheduledExecutorService** — Daemon thread with fixed delay to prevent overlap
- **Platform.setImplicitExit(false)** — Keeps app alive when window is hidden

## 📝 License

This project is licensed under the MIT License.
