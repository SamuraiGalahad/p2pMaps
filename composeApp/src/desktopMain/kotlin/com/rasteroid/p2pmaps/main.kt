package com.rasteroid.p2pmaps

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.config.AppConfig
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.p2p.listen
import com.rasteroid.p2pmaps.raster.ExternalRasterRepository
import com.rasteroid.p2pmaps.ui.App
import kotlinx.coroutines.*
import kotlin.concurrent.fixedRateTimer

private val log = Logger.withTag("main")

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    log.i("Starting p2p listener")
    val listenJob = GlobalScope.launch(Dispatchers.IO) {
        listen(Settings.APP_CONFIG.listenerPort)
    }

    val rastersRefreshJob = fixedRateTimer(
        name = "rastersRefresh",
        initialDelay = 0,
        period = 120_000
    ) {
        ExternalRasterRepository.instance.refresh(GlobalScope)
    }

    log.i("Starting main application")
    application {
        Window(
            onCloseRequest = {
                log.i("Received application close request")
                listenJob.cancel()
                rastersRefreshJob.cancel()
                exitApplication()
            },
            title = Settings.APP_NAME,
        ) {
            App()
        }
    }
}