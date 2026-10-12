#!/usr/bin/env bash
# SportsPulse Universal Build Script (Linux / macOS / Git Bash)
# Dependency-free: requires only a standard JDK 17+ (javac, java, jar).
#
# Usage:
#   ./build.sh build     # compile sources into bin/
#   ./build.sh test      # compile and run the 165+ assertion self-test suite
#   ./build.sh run       # compile and launch interactive SportsPulse
#   ./build.sh jar       # package into dist/SportsPulse.jar
#   ./build.sh check     # run automated portability & environment check
#   ./build.sh clean     # remove compiled classes and build output

set -euo pipefail
cd "$(dirname "$0")"

compile() {
    mkdir -p bin
    mkdir -p runtime/logs runtime/reports runtime/benchmarks runtime/data
    if [ -d "src" ]; then
        find src -name "*.java" > sources.txt
    else
        find sportspulse/src -name "*.java" > sources.txt
    fi
    javac -encoding UTF-8 -d bin @sources.txt
    rm -f sources.txt
}

case "${1:-build}" in
    build)
        compile
        echo "Build OK -> bin/"
        ;;
    test)
        compile
        java -Dfile.encoding=UTF-8 -cp bin com.sportspulse.SelfTest
        ;;
    run)
        compile
        java -Dfile.encoding=UTF-8 -cp bin com.sportspulse.Main
        ;;
    check)
        compile
        java -Dfile.encoding=UTF-8 -cp bin com.sportspulse.EnvironmentCheck
        ;;
    jar)
        compile
        mkdir -p dist
        mkdir -p bin/META-INF
        printf 'Manifest-Version: 1.0\nMain-Class: com.sportspulse.Main\nImplementation-Title: SportsPulse\n' > bin/META-INF/MANIFEST.MF
        jar cfm dist/SportsPulse.jar bin/META-INF/MANIFEST.MF -C bin .
        cp -f dist/SportsPulse.jar SportsPulse.jar
        echo "Built standalone runnable JAR -> dist/SportsPulse.jar and SportsPulse.jar"
        ;;
    clean)
        rm -rf bin build dist sources.txt
        echo "Cleaned."
        ;;
    *)
        echo "Usage: $0 {build|test|run|jar|check|clean}" >&2
        exit 1
        ;;
esac
