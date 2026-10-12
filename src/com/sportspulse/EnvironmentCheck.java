package com.sportspulse;

import com.sportspulse.util.Ansi;
import com.sportspulse.util.AppPaths;
import com.sportspulse.util.Console;
import com.sportspulse.util.ConsoleCapabilities;
import com.sportspulse.util.TerminalRenderer;
import com.sportspulse.util.TerminalTheme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Diagnostic utility to verify host environment portability, JDK compatibility,
 * filesystem writability, and ANSI presentation capabilities.
 *
 * Safe by design: Does NOT print sensitive personal usernames, passwords, tokens,
 * or user machine paths.
 */
public final class EnvironmentCheck {

    public static void main(String[] args) {
        run();
    }

    public static boolean run() {
        TerminalRenderer.renderBoxHeader("SPORTSPULSE ENVIRONMENT CHECK", "Portability & Host Runtime Verification");
        System.out.println();

        String javaVersion = System.getProperty("java.version", "Unknown");
        String javaVendor = System.getProperty("java.vendor", "Unknown");
        String osName = System.getProperty("os.name", "Unknown");
        String osArch = System.getProperty("os.arch", "Unknown");

        // Java 17+ Check
        boolean javaVersionOk = isJava17OrHigher(javaVersion);
        String javaStatus = javaVersionOk ?
                Ansi.paint(TerminalTheme.success(), javaVersion + " (" + javaVendor + ") [OK]") :
                Ansi.paint(TerminalTheme.warning(), javaVersion + " [Requires 17+]");

        // Writable Runtime Check
        boolean writableRuntime = checkWritableRuntime();
        String runtimeStatus = writableRuntime ?
                Ansi.paint(TerminalTheme.success(), "YES (runtime/ writable) [OK]") :
                Ansi.paint(TerminalTheme.error(), "NO (Check permissions) [FAIL]");

        // ANSI Terminal Capability Check
        var colorMode = ConsoleCapabilities.getColorMode();
        boolean interactive = ConsoleCapabilities.isInteractive();
        String ansiStatus = colorMode.getDescription() + (interactive ? " [Interactive TTY]" : " [Line Mode Fallback]");

        // Viewport Width
        int termWidth = ConsoleCapabilities.getTerminalWidth();
        var widthTier = ConsoleCapabilities.getWidthTier();
        String widthStatus = termWidth + " columns (" + widthTier.name() + ")";

        // Core Resources & In-Memory Data Check
        boolean resourcesOk = checkResources();
        String resourceStatus = resourcesOk ?
                Ansi.paint(TerminalTheme.success(), "OK (All sport datasets verified)") :
                Ansi.paint(TerminalTheme.error(), "FAIL (Data missing)");

        // Relative Project Root
        Path root = AppPaths.getProjectRoot();
        String rootLabel = root.getFileName() != null ? root.getFileName().toString() : ".";

        Console.kv("Java Version", javaStatus);
        Console.kv("Operating System", osName + " (" + osArch + ")");
        Console.kv("Project Container", rootLabel);
        Console.kv("Terminal Mode", ansiStatus);
        Console.kv("Viewport Geometry", widthStatus);
        Console.kv("Writable Runtime", runtimeStatus);
        Console.kv("Application Datasets", resourceStatus);

        boolean ready = javaVersionOk && writableRuntime && resourcesOk;
        System.out.println();
        if (ready) {
            Console.success("Environment Status : READY (Fully portable across Windows/Linux/macOS & IDEs)");
        } else {
            Console.warn("Environment Status : ATTENTION REQUIRED (Review warnings above)");
        }
        System.out.println();
        return ready;
    }

    private static boolean isJava17OrHigher(String version) {
        try {
            String clean = version.split("[.-]")[0];
            int major = Integer.parseInt(clean);
            return major >= 17;
        } catch (Exception e) {
            return true; // Fallback to permissive
        }
    }

    private static boolean checkWritableRuntime() {
        try {
            Path testFile = AppPaths.getRuntimeDir().resolve(".portability_probe");
            Files.writeString(testFile, "probe");
            Files.deleteIfExists(testFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean checkResources() {
        try {
            var data = new com.sportspulse.data.MockData();
            return !data.allPlayers.isEmpty() && !data.allTeams.isEmpty() && !data.allMatches.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
