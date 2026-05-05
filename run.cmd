@echo off
title PingGuard - Network Latency Monitor
echo.
echo  ========================================
echo   PingGuard v1.0 - Starting Application
echo  ========================================
echo.

REM === Set JAVA_HOME ===
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"

REM === Verify Java exists ===
if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java tidak ditemukan di: %JAVA_HOME%
    echo         Install JDK 17+ dari https://adoptium.net/
    pause
    exit /b 1
)
echo [OK] Java : %JAVA_HOME%

REM === Set Maven path ===
set "MVN_CMD=%~dp0tools\apache-maven-3.9.15\bin\mvn.cmd"
if not exist "%MVN_CMD%" (
    echo [ERROR] Maven tidak ditemukan di: %MVN_CMD%
    echo         Jalankan "setup.cmd" terlebih dahulu.
    pause
    exit /b 1
)
echo [OK] Maven: %~dp0tools\apache-maven-3.9.15

REM === JVM Memory Optimization Flags ===
REM Serial GC  = lowest memory overhead for small apps
REM Xms32m     = start with only 32MB heap
REM Xmx96m     = max 96MB heap (enough for JavaFX + our logic)
REM TieredStopAtLevel=1 = skip expensive C2 compiler, saves ~30MB code cache
set "MAVEN_OPTS=-Xms32m -Xmx96m -XX:+UseSerialGC -XX:MaxMetaspaceSize=64m -XX:ReservedCodeCacheSize=32m -XX:+TieredCompilation -XX:TieredStopAtLevel=1"

echo [OK] Memory: Optimized (Heap 32-96MB, SerialGC)
echo.
echo [....] Compiling dan menjalankan PingGuard...
echo.

REM === Run the application ===
call "%MVN_CMD%" -f "%~dp0pom.xml" javafx:run

if errorlevel 1 (
    echo.
    echo [ERROR] Gagal menjalankan aplikasi. Cek error di atas.
    pause
)
