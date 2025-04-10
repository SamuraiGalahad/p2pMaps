package com.rasteroid.p2pmaps.vm

import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.ui.ProgressLayerTMS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.concurrent.fixedRateTimer

class DownloadRastersViewModel : ViewModel() {
    private val _rasters = MutableStateFlow(listOf<ProgressLayerTMS>())
    val rasters: StateFlow<List<ProgressLayerTMS>> = _rasters

    init {
        fixedRateTimer(
            name = "Layers progress refresh job",
            period = 2000,
            initialDelay = 1000) {
            val layerTMSs = TileRepository.instance.getLayerTMSs()
            val progressLayers = layerTMSs.map { layerTMS ->
                val progress = TileRepository.instance.getLayerProgress(
                    layerTMS.layer,
                    layerTMS.tileMatrixSet
                )!!
                ProgressLayerTMS(
                    layerTMS,
                    progress.first,
                    progress.second
                )
            }
            _rasters.value = progressLayers
        }
    }
}