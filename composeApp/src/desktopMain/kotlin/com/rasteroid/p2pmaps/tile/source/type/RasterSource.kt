package com.rasteroid.p2pmaps.tile.source.type

import com.rasteroid.p2pmaps.tile.LayerTMS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class RasterSource(
    val name: String,
    val type: RasterSourceType
) {
    protected val _rasters = MutableStateFlow(listOf<LayerTMS>())
    val rasters: StateFlow<List<LayerTMS>> = _rasters

    abstract suspend fun refresh()

    abstract suspend fun download(
        layerTMS: LayerTMS,
        // Received tiles out of all tiles.
        progressReport: (Int, Int) -> Unit
    )
}