package com.rasteroid.p2pmaps.raster.source.type

import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import java.io.OutputStream

interface RasterSource {
    val name: String
    val type: RasterSourceType

    fun fetch(onRasterFound: (RasterMeta) -> Unit)

    fun download(resultStream: OutputStream, raster: RasterMeta)
}