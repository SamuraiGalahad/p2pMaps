package com.rasteroid.p2pmaps.raster.source.type

import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import com.rasteroid.p2pmaps.raster.meta.RasterSourceMetaReply
import java.io.OutputStream

interface RasterSource {
    val name: String
    val type: RasterSourceType

    fun fetch(onRasterFound: (RasterSourceMetaReply) -> Unit)

    fun download(resultStream: OutputStream, raster: RasterMeta)
}