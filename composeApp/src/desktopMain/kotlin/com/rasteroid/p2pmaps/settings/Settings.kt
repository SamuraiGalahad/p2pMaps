package com.rasteroid.p2pmaps.settings

object Settings {
    const val APP_NAME = "p2pmaps"
    val APP_CONFIG_PATH = getConfigDirectory(APP_NAME)

    init {
        ensureDirectoryExists(APP_CONFIG_PATH)
    }
}