package com.rasteroid.p2pmaps.tile.source

import com.rasteroid.p2pmaps.tile.source.type.RasterSource

data class DownloadableRasterMeta(
    val source: RasterSource,
    val meta: RasterSourceMetaReply
)