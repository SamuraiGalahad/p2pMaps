package com.rasteroid.p2pmaps.vm

import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.tile.ExternalRasterRepository
import com.rasteroid.p2pmaps.tile.SourcedLayerTMS
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ExternalRastersViewModel : ViewModel() {
    val rasters = ExternalRasterRepository.instance.rasters

    @OptIn(DelicateCoroutinesApi::class)
    fun onDownloadSource(sourcedLayerTMS: SourcedLayerTMS) {
        GlobalScope.launch {
            sourcedLayerTMS.source.download(sourcedLayerTMS.layerTMS) {
                current, total ->
            }
        }
    }
}