@echo off
title PingGuard - Build
echo.
echo  ========================================
echo   PingGuard v1.0 - Building JAR
echo  ========================================
echo.

REM === Set JAVA_HOME ===
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java tidak ditemukan di: %JAVA_HOME%
    pause
    exit /b 1
)

REM === Set Maven path ===
set "MVN_CMD=%~dp0tools\apache-maven-3.9.15\bin\mvn.cmd"
if not exist "%MVN_CMD%" (
    echo [ERROR] Maven tidak ditemukan.
    pause
    exit /b 1
)

echo [....] Building PingGuard...
echo.

call "%MVN_CMD%" -f "%~dp0pom.xml" clean package -DskipTests

if errorlevel 1 (
    echo.
    echo [ERROR] Build gagal.
    pause
    exit /b 1
)

echo.
echo  ========================================
echo   BUILD SUKSES!
echo   JAR file: target\pingguard-1.0-SNAPSHOT.jar
echo  ========================================
echo.
pause
