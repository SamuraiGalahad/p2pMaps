package com.rasteroid.p2pmaps

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.p2p.listen
import com.rasteroid.p2pmaps.tile.ExternalRasterRepository
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.server.WMTSServer
import com.rasteroid.p2pmaps.ui.App
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.concurrent.fixedRateTimer

private val log = Logger.withTag("main")

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    Logger.addLogWriter(lastLogs)

    log.i("Starting p2p listener")
    val listenJob = GlobalScope.launch(Dispatchers.IO) {
        listen(Settings.APP_CONFIG.listenerPort)
    }

    val rastersRefreshJob = fixedRateTimer(
        name = "rastersRefresh",
        initialDelay = 5_000,
        period = 120_000
    ) {
        ExternalRasterRepository.instance.refresh(GlobalScope)
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
                listenJob.cancel()
                rastersRefreshJob.cancel()
                exitApplication()
                server.stop()
            },
            title = Settings.APP_NAME,
        ) {
            App()
        }
    }
}