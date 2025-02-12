package com.rasteroid.p2pmaps.config

import co.touchlab.kermit.Logger
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.peanuuutz.tomlkt.Toml
import java.nio.file.Path
import kotlin.io.path.writeText

private val log = Logger.withTag("settings")

enum class Platform {
    WINDOWS,
    MACOS,
    LINUX
}

object Settings {
    const val APP_NAME = "p2pmaps"
    val PLATFORM = when {
        System.getProperty("os.name").lowercase().contains("windows") -> Platform.WINDOWS
        System.getProperty("os.name").lowercase().contains("macos") -> Platform.MACOS
        else -> Platform.LINUX // Assume Linux
    }
    val PLATFORM_HOME_PATH: String = System.getProperty("user.home")
    val APP_CONFIG_PATH = getConfigDirectory(APP_NAME)
    val APP_DATA_PATH = getDataDirectory(APP_NAME)
    val APP_CONFIG_FILE_PATH: Path = APP_CONFIG_PATH.resolve("config.toml")

    init {
        log.i("App config path: $APP_CONFIG_PATH")
        log.i("App data path: $APP_DATA_PATH")
        ensureDirectoryExists(APP_CONFIG_PATH)
        ensureDirectoryExists(APP_DATA_PATH)
    }

    val APP_CONFIG = parseFromFileOrDefault(
        path = APP_CONFIG_FILE_PATH,
        encoder = { Toml.encodeToString(it) },
        decoder = { Toml.decodeFromString(it) }) {
        AppConfig()
    }

    fun writeAppConfig() {
        runCatching {
            APP_CONFIG_FILE_PATH.writeText(Toml.encodeToString(APP_CONFIG))
        }.onFailure {
            log.e("Failed to write app config", it)
        }
    }
}