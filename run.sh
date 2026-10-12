#!/usr/bin/env bash
# SportsPulse Cross-Platform Linux/macOS Launcher
# Fully location-independent: detects dist/SportsPulse.jar or builds on demand.

set -euo pipefail
cd "$(dirname "$0")"

if [ -f "dist/SportsPulse.jar" ]; then
    exec java -Dfile.encoding=UTF-8 -jar dist/SportsPulse.jar "$@"
fi

if [ -f "SportsPulse.jar" ]; then
    exec java -Dfile.encoding=UTF-8 -jar SportsPulse.jar "$@"
fi

echo "[SportsPulse] Packaging executable distribution JAR..."
if [ -f "./build.sh" ]; then
    chmod +x ./build.sh
    ./build.sh jar
fi

if [ -f "dist/SportsPulse.jar" ]; then
    exec java -Dfile.encoding=UTF-8 -jar dist/SportsPulse.jar "$@"
else
    echo "[ERROR] Failed to launch. Please verify that JDK 17+ is installed." >&2
    exit 1
fi
