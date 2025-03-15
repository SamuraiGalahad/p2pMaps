package com.rasteroid.p2pmaps.tile.source.type

import com.rasteroid.p2pmaps.tile.RasterMeta
import com.rasteroid.p2pmaps.tile.TileMeta
import java.io.OutputStream

interface RasterSource {
    val name: String
    val type: RasterSourceType

    fun fetch(onLayerFound: (RasterMeta) -> Unit)

    fun download(
        resultStream: OutputStream,
        rasterMeta: RasterMeta,
        onDataStart: (Long) -> Unit
    )
}