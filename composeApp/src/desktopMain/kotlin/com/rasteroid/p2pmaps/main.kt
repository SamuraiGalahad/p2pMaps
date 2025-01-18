package com.rasteroid.p2pmaps

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.rasteroid.p2pmaps.ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "p2pmaps",
    ) {
        App()
    }
}