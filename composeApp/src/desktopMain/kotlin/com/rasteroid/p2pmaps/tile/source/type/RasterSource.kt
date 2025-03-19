package com.rasteroid.p2pmaps.tile.source.type

import com.rasteroid.p2pmaps.tile.RasterMeta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class RasterSource(
    val name: String,
    val type: RasterSourceType
) {
    protected val _rasters = MutableStateFlow(listOf<RasterMeta>())
    val rasters: StateFlow<List<RasterMeta>> = _rasters

    abstract suspend fun refresh()

    abstract suspend fun download(
        rasterMeta: RasterMeta,
        // Received tiles out of all tiles.
        progressReport: (Int, Int) -> Unit
    )
}