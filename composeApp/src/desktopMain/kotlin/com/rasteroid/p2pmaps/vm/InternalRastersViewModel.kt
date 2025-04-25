package com.rasteroid.p2pmaps.vm

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.server.ProgressLayerTMS
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.tile.TileFormat
import kotlin.concurrent.fixedRateTimer

class InternalRastersViewModel : ViewModel() {
    val rasters = mutableStateListOf<ProgressLayerTMS>()

    private fun getTMSTileCount(tms: String): Int {
        TileRepository.instance.getTMSMeta(tms)?.let { tmsMeta ->
            return tmsMeta.tileMatrixes.sumOf { tileMatrix ->
                tileMatrix.matrixWidth * tileMatrix.matrixHeight
            }
        } ?: return 0
    }

    private fun updateRasters() {
        val layerTMSs = TileRepository.instance.layers
        val uniqueTileMatrixSets = layerTMSs.map { it.tileMatrixSet }.distinct()
        // Map TMS into the number of tiles.
        val tmsTileCount = uniqueTileMatrixSets.associateWith { tms ->
            getTMSTileCount(tms)
        }

        // For each layer+TMS pair, find it's progress.
        // We already know total tiles of each TMS.
        val progressLayers = layerTMSs.map { layerTMS ->
            val tiles = TileRepository.instance.getLayerTileCount(
                layerTMS.layer,
                layerTMS.tileMatrixSet,
                TileFormat.PNG.getExtension()
            )
            ProgressLayerTMS(
                layerTMS,
                tiles,
                tmsTileCount[layerTMS.tileMatrixSet] ?: 0,
            )
        }

        // Update rasters with new values.
        rasters.clear()
        rasters.addAll(progressLayers)
    }

    init {
        fixedRateTimer(
            name = "Layers progress refresh job",
            period = 2000,
            initialDelay = 1000) {
            updateRasters()
        }
    }
}