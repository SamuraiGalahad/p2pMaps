package com.rasteroid.p2pmaps.vm

import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.server.TileRepository

class DownloadRastersViewModel : ViewModel() {
    val rasters
        get() = TileRepository.instance.getRasters()
}