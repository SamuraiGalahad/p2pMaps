package com.rasteroid.p2pmaps.config

import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Returns a platform-dependent config directory path for the given appName.
 * For instance:
 *  - Linux:   ~/.config/<appName>   or XDG_CONFIG_HOME/<appName> if set
 *  - macOS:   ~/Library/Application Support/<appName>
 *  - Windows: %APPDATA%\<appName>
 */
fun getConfigDirectory(appName: String): Path {
    return when(Settings.PLATFORM) {
        Platform.WINDOWS -> {
            // On Windows, prefer APPDATA if available, else fallback to user.home
            val appData = System.getenv("APPDATA")
            if (appData.isNullOrBlank()) {
                Paths.get(System.getProperty("user.home"), "AppData", "Roaming", appName)
            } else {
                Paths.get(appData, appName)
            }
        }
        Platform.MACOS -> {
            // macOS convention
            Paths.get(System.getProperty("user.home"), "Library", "Application Support", appName)
        }
        Platform.LINUX -> {
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

fun getDataDirectory(appName: String): Path {
    return when(Settings.PLATFORM) {
        Platform.WINDOWS -> {
            // On Windows, prefer APPDATA if available, else fallback to user.home
            val appData = System.getenv("APPDATA")
            if (appData.isNullOrBlank()) {
                Paths.get(System.getProperty("user.home"), "AppData", "Roaming", appName)
            } else {
                Paths.get(appData, appName)
            }
        }
        Platform.MACOS -> {
            // macOS convention
            Paths.get(System.getProperty("user.home"), "Library", "Application Support", appName)
        }
        Platform.LINUX -> {
            // Linux / Unix
            val xdgDataHome = System.getenv("XDG_DATA_HOME")
            if (!xdgDataHome.isNullOrBlank()) {
                Paths.get(xdgDataHome, appName)
            } else {
                // fallback to ~/.local/share/<appName>
                Paths.get(System.getProperty("user.home"), ".local", "share", appName)
            }
        }
    }
}

fun ensureFileExists(path: Path) {
    if (!path.toFile().exists()) {
        path.toFile().createNewFile()
    }
}

fun ensureDefaultFileExists(
    path: Path,
    defaultContent: () -> String
) {
    if (!path.toFile().exists()) {
        path.toFile().createNewFile()
        path.writeText(defaultContent())
    }
}

fun <T> parseFromFileOrDefault(
    path: Path,
    encoder: (T) -> String,
    decoder: (String) -> T,
    default: () -> T
): T {
    // Try to parse file and if successful, return the parsed object.
    // If not successful, try to create the file and write to it the default content.
    // If that fails, return the default object.
    return runCatching {
        decoder(path.readText())
    }.getOrDefault(runCatching {
        ensureDefaultFileExists(path) {
            encoder(default())
        }
        default()
    }.getOrDefault(default()))
}

fun ensureDirectoryExists(path: Path) {
    if (!path.toFile().exists()) {
        path.toFile().mkdirs()
    }
}