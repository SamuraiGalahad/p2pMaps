package com.rasteroid.p2pmaps.vm

import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.tile.ExternalRasterRepository
import com.rasteroid.p2pmaps.tile.source.type.RasterSource
import com.rasteroid.p2pmaps.ui.SourcedLayerTMS
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExternalRastersViewModel : ViewModel() {
    val sources: StateFlow<List<RasterSource>> = ExternalRasterRepository.instance.sources

    @OptIn(DelicateCoroutinesApi::class)
    fun onDownloadSource(sourcedLayerTMS: SourcedLayerTMS) {
        GlobalScope.launch {
            sourcedLayerTMS.source.download(sourcedLayerTMS.layerTMS) {
                current, total ->
            }
        }
    }
}