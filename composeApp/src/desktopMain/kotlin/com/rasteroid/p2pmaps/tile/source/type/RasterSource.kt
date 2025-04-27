package com.rasteroid.p2pmaps.tile.source.type

import com.rasteroid.p2pmaps.tile.LayerTMS
import kotlinx.coroutines.CoroutineScope

abstract class RasterSource(
    val name: String,
    val type: RasterSourceType
) {
    var isAlive = true
        protected set

    abstract suspend fun getRasters(): Result<List<LayerTMS>>

    abstract suspend fun download(
        layerTMS: LayerTMS,
        // Received tiles out of all tiles.
        progressReport: (Int, Int) -> Unit
    )

    open fun startBackground(scope: CoroutineScope) {}
}