package com.rasteroid.p2pmaps

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.server.WMTSServer
import com.rasteroid.p2pmaps.tile.ExternalRasterRepository
import com.rasteroid.p2pmaps.tile.source.type.TrackerRasterSource
import com.rasteroid.p2pmaps.ui.App

private val log = Logger.withTag("main")

fun main() {
    Logger.addLogWriter(lastLogs)

    // Set up tracker.
    for (trackerUrl in Settings.APP_CONFIG.trackerUrls) {
        ExternalRasterRepository.instance.addSource(
            TrackerRasterSource(
                trackerUrl
            )
        )
    }

    val server = WMTSServer(
        port = Settings.APP_CONFIG.localWMTSServerPort,
        prefix = "/wmts",
        tileRepository = TileRepository.instance
    )
    server.start()

    log.i("Starting main application")
    application {
        Window(
            onCloseRequest = {
                log.i("Received application close request")
                ExternalRasterRepository.instance.cancel()
                server.stop()
                exitApplication()
            },
            title = Settings.APP_NAME,
        ) {
            App()
        }
    }
}