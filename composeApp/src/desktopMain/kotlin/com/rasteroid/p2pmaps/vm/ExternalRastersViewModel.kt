package com.rasteroid.p2pmaps.vm

import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.ExternalRasterRepository
import com.rasteroid.p2pmaps.tile.SourcedLayerTMS
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private val log = Logger.withTag("external raster vm")

class ExternalRastersViewModel : ViewModel() {
    val rasters = ExternalRasterRepository.instance.rasters

    @OptIn(DelicateCoroutinesApi::class)
    fun onDownloadSource(sourcedLayerTMS: SourcedLayerTMS) {
        GlobalScope.launch {
            log.d("Downloading source: ${sourcedLayerTMS.source.name}")
            sourcedLayerTMS.source.download(sourcedLayerTMS.layerTMS) {
                current, total ->
            }
        }
    }
}