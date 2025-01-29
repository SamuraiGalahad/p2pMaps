package com.rasteroid.p2pmaps.settings

import java.nio.file.Path
import java.nio.file.Paths

/**
 * Returns a platform-dependent config directory path for the given appName.
 * For instance:
 *  - Linux:   ~/.config/<appName>   or XDG_CONFIG_HOME/<appName> if set
 *  - macOS:   ~/Library/Application Support/<appName>
 *  - Windows: %APPDATA%\<appName>
 */
fun getConfigDirectory(appName: String): Path {
    val os = System.getProperty("os.name").lowercase()
    return when {
        os.contains("win") -> {
            // On Windows, prefer APPDATA if available, else fallback to user.home
            val appData = System.getenv("APPDATA")
            if (appData.isNullOrBlank()) {
                Paths.get(System.getProperty("user.home"), "AppData", "Roaming", appName)
            } else {
                Paths.get(appData, appName)
            }
        }
        os.contains("mac") -> {
            // macOS convention
            Paths.get(System.getProperty("user.home"), "Library", "Application Support", appName)
        }
        else -> {
            // Linux / Unix
            val xdgConfigHome = System.getenv("XDG_CONFIG_HOME")
            if (!xdgConfigHome.isNullOrBlank()) {
                Paths.get(xdgConfigHome, appName)
            } else {
                // fallback to ~/.config/<appName>
                Paths.get(System.getProperty("user.home"), ".config", appName)
            }
        }
    }
}

fun ensureFileExists(path: Path) {
    if (!path.toFile().exists()) {
        path.toFile().createNewFile()
    }
}

fun ensureDirectoryExists(path: Path) {
    if (!path.toFile().exists()) {
        path.toFile().mkdirs()
    }
}