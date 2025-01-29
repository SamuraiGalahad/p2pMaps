package com.rasteroid.p2pmaps.raster

// SourcedRaster maybe? Idk it's a raster meta + source info.
data class SourcedRasterMeta(
    val sourceId: Int,
    val sourceName: String,
    val sourceType: RasterSourceType,
    val meta: RasterMeta
)