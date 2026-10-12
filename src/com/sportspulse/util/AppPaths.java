package com.sportspulse.util;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centralized, location-independent path resolution for SportsPulse.
 *
 * Guarantees that:
 * 1. ZERO machine-specific, absolute, or user-dependent paths are used.
 * 2. Application root is resolved dynamically whether launched from:
 *    - Project root directory
 *    - bin/ or dist/ subdirectories
 *    - External working directories (e.g. java -jar /anywhere/dist/SportsPulse.jar)
 *    - Any IDE (IntelliJ IDEA, Eclipse, VS Code, NetBeans)
 * 3. Writable runtime directories (logs, reports, benchmarks, data) are managed centrally
 *    and created automatically on demand.
 * 4. System property ("sportspulse.home") or Environment variable ("SPORTSPULSE_HOME")
 *    can optionally override base path if desired.
 */
public final class AppPaths {

    private static final String RUNTIME_DIR_NAME = "runtime";
    private static final String LOGS_DIR_NAME = "logs";
    private static final String REPORTS_DIR_NAME = "reports";
    private static final String BENCHMARKS_DIR_NAME = "benchmarks";
    private static final String DATA_DIR_NAME = "data";
    private static final String CONFIG_DIR_NAME = "config";
    private static final String RESOURCES_DIR_NAME = "resources";

    private static Path cachedProjectRoot = null;

    private AppPaths() {}

    /**
     * Resolves the authoritative project root directory.
     */
    public static synchronized Path getProjectRoot() {
        if (cachedProjectRoot != null) {
            return cachedProjectRoot;
        }

        // 1. Explicit override via System Property
        String sysProp = System.getProperty("sportspulse.home");
        if (sysProp != null && !sysProp.isBlank()) {
            cachedProjectRoot = Paths.get(sysProp.trim()).toAbsolutePath().normalize();
            return cachedProjectRoot;
        }

        // 2. Explicit override via Environment Variable
        String envProp = System.getenv("SPORTSPULSE_HOME");
        if (envProp != null && !envProp.isBlank()) {
            cachedProjectRoot = Paths.get(envProp.trim()).toAbsolutePath().normalize();
            return cachedProjectRoot;
        }

        // 3. Dynamic resolution from CodeSource location (JAR or class directory)
        Path fromCode = resolveFromCodeSource();
        if (fromCode != null && isValidProjectRoot(fromCode)) {
            cachedProjectRoot = fromCode;
            return cachedProjectRoot;
        }

        // 4. Working directory inspection
        Path userDir = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        if (isValidProjectRoot(userDir)) {
            cachedProjectRoot = userDir;
            return cachedProjectRoot;
        }

        // Check if user.dir is inside bin or dist
        if (userDir.getParent() != null && isValidProjectRoot(userDir.getParent())) {
            cachedProjectRoot = userDir.getParent();
            return cachedProjectRoot;
        }

        // Default to user.dir if marker files are not yet generated
        cachedProjectRoot = (fromCode != null) ? fromCode : userDir;
        return cachedProjectRoot;
    }

    private static Path resolveFromCodeSource() {
        try {
            var codeSource = AppPaths.class.getProtectionDomain().getCodeSource();
            if (codeSource == null || codeSource.getLocation() == null) {
                return null;
            }
            URI uri = codeSource.getLocation().toURI();
            Path location = Paths.get(uri).toAbsolutePath().normalize();

            if (Files.isRegularFile(location)) {
                // We are inside a JAR file (e.g. dist/SportsPulse.jar or SportsPulse.jar)
                Path parent = location.getParent();
                if (parent != null) {
                    if ("dist".equalsIgnoreCase(parent.getFileName().toString()) ||
                        "bin".equalsIgnoreCase(parent.getFileName().toString()) ||
                        "build".equalsIgnoreCase(parent.getFileName().toString())) {
                        return parent.getParent() != null ? parent.getParent() : parent;
                    }
                    return parent;
                }
            } else if (Files.isDirectory(location)) {
                // Running from exploded classes directory (e.g. bin/, build/classes/)
                Path parent = location.getParent();
                if (parent != null) {
                    if ("bin".equalsIgnoreCase(location.getFileName().toString())) {
                        return parent;
                    }
                    if ("classes".equalsIgnoreCase(location.getFileName().toString()) && parent.getParent() != null) {
                        return parent.getParent();
                    }
                    return parent;
                }
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean isValidProjectRoot(Path candidate) {
        if (candidate == null || !Files.isDirectory(candidate)) return false;
        return Files.exists(candidate.resolve("src")) ||
               Files.exists(candidate.resolve("build.xml")) ||
               Files.exists(candidate.resolve("config")) ||
               Files.exists(candidate.resolve("runtime")) ||
               Files.exists(candidate.resolve("resources")) ||
               Files.exists(candidate.resolve("sportspulse"));
    }

    /**
     * Central runtime folder for generated files.
     */
    public static Path getRuntimeDir() {
        return ensureDir(getProjectRoot().resolve(RUNTIME_DIR_NAME));
    }

    public static Path getLogsDir() {
        return ensureDir(getRuntimeDir().resolve(LOGS_DIR_NAME));
    }

    public static Path getReportsDir() {
        return ensureDir(getRuntimeDir().resolve(REPORTS_DIR_NAME));
    }

    public static Path getBenchmarksDir() {
        return ensureDir(getRuntimeDir().resolve(BENCHMARKS_DIR_NAME));
    }

    public static Path getDataDir() {
        return ensureDir(getRuntimeDir().resolve(DATA_DIR_NAME));
    }

    public static Path getConfigDir() {
        return ensureDir(getProjectRoot().resolve(CONFIG_DIR_NAME));
    }

    public static Path getResourcesDir() {
        return ensureDir(getProjectRoot().resolve(RESOURCES_DIR_NAME));
    }

    public static Path resolveBenchmarkCsv(String filename) {
        String safeName = (filename == null || filename.isBlank()) ? "benchmark_results.csv" : filename.trim();
        Path p = Paths.get(safeName);
        if (p.isAbsolute()) {
            ensureDir(p.getParent());
            return p;
        }
        return getBenchmarksDir().resolve(p.getFileName());
    }

    public static Path resolveReportFile(String filename) {
        return getReportsDir().resolve(filename);
    }

    public static Path resolveLogFile(String filename) {
        return getLogsDir().resolve(filename);
    }

    public static Path resolveConfigFile(String filename) {
        return getConfigDir().resolve(filename);
    }

    private static Path ensureDir(Path dir) {
        if (dir != null && !Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException ignored) {}
        }
        return dir;
    }

    /**
     * Initializes all standard runtime directories.
     */
    public static void initializeRuntimeDirectories() {
        getRuntimeDir();
        getLogsDir();
        getReportsDir();
        getBenchmarksDir();
        getDataDir();
        getConfigDir();
        getResourcesDir();
    }
}
