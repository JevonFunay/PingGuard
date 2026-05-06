@echo off
title PingGuard - Build EXE
echo.
echo  ========================================
echo   PingGuard - Building Native EXE
echo  ========================================
echo.

REM === Set JAVA_HOME ===
set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-25.0.1.8-hotspot"

if not exist "%JAVA_HOME%\bin\jpackage.exe" (
    echo [ERROR] jpackage tidak ditemukan di: %JAVA_HOME%\bin
    pause
    exit /b 1
)

REM === Set Maven path ===
set "MVN_CMD=%~dp0tools\apache-maven-3.9.15\bin\mvn.cmd"
if not exist "%MVN_CMD%" (
    echo [ERROR] Maven tidak ditemukan di: %MVN_CMD%
    pause
    exit /b 1
)

echo [1/2] Membangun JAR file...
call "%MVN_CMD%" -f "%~dp0pom.xml" package -DskipTests

if errorlevel 1 (
    echo.
    echo [ERROR] Build JAR gagal.
    pause
    exit /b 1
)

echo.
echo [2/2] Membangun .exe (Native App Image) menggunakan jpackage...
echo Proses ini akan membundel Java (JRE) ke dalam aplikasi.
echo Mohon tunggu, mungkin memakan waktu beberapa saat...

if exist "%~dp0dist\PingGuard" (
    rmdir /s /q "%~dp0dist\PingGuard"
)

"%JAVA_HOME%\bin\jpackage.exe" ^
  --type app-image ^
  --name PingGuard ^
  --input "%~dp0target" ^
  --main-jar pingguard-1.0-SNAPSHOT.jar ^
  --main-class com.pingguard.Launcher ^
  --dest "%~dp0dist" ^
  --java-options "-Xms32m" ^
  --java-options "-Xmx96m" ^
  --java-options "-XX:+UseSerialGC" ^
  --java-options "--add-exports" ^
  --java-options "java.desktop/sun.awt=ALL-UNNAMED" ^
  --java-options "--enable-native-access=ALL-UNNAMED" ^
  --java-options "--sun-misc-unsafe-memory-access=allow"

if errorlevel 1 (
    echo.
    echo [ERROR] Pembuatan EXE gagal.
    pause
    exit /b 1
)

echo.
echo  ========================================
echo   SUKSES!
echo   Aplikasi mandiri (standalone) berhasil dibuat.
echo.
echo   Lokasi: %~dp0dist\PingGuard\PingGuard.exe
echo.
echo   Anda bisa mendistribusikan folder "dist\PingGuard" ini
echo   ke komputer Windows lain, dan mereka bisa langsung
echo   menjalankannya TANPA perlu menginstal Java!
echo  ========================================
echo.
pause
