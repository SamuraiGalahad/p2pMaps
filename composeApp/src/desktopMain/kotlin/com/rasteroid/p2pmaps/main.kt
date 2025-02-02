package com.rasteroid.p2pmaps

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.listen
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.ui.App
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private val log = Logger.withTag("main")

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    log.i("Starting p2p listener")
    val listenJob = GlobalScope.launch(Dispatchers.IO) {
        listen(42069)
    }

    log.i("Starting main application")
    application {
        Window(
            onCloseRequest = {
                log.i("Received application close request")
                listenJob.cancel()
                exitApplication()
            },
            title = Settings.APP_NAME,
        ) {
            App()
        }
    }
}