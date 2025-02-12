package com.rasteroid.p2pmaps.raster.source

import com.rasteroid.p2pmaps.raster.meta.RasterSourceMetaReply
import com.rasteroid.p2pmaps.raster.source.type.RasterSource

data class DownloadableRasterMeta(
    val source: RasterSource,
    val meta: RasterSourceMetaReply
)