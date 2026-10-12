@echo off
setlocal
cd /d "%~dp0"

REM SportsPulse Cross-Platform Windows Launcher
REM Fully location-independent: detects dist\SportsPulse.jar or builds on demand.

if exist "dist\SportsPulse.jar" (
    java -Dfile.encoding=UTF-8 -jar dist\SportsPulse.jar %*
    goto :eof
)

if exist "SportsPulse.jar" (
    java -Dfile.encoding=UTF-8 -jar SportsPulse.jar %*
    goto :eof
)

echo [SportsPulse] Packaging executable distribution JAR...
call build.bat jar
if exist "dist\SportsPulse.jar" (
    java -Dfile.encoding=UTF-8 -jar dist\SportsPulse.jar %*
) else (
    echo [ERROR] Failed to launch. Please verify that JDK 17+ is installed.
)
