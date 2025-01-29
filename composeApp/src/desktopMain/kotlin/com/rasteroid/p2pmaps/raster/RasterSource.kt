package com.rasteroid.p2pmaps.raster

interface RasterSource {
    val name: String
    val type: RasterSourceType

    suspend fun fetchRasters(): List<RasterMeta>

    suspend fun downloadRaster(raster: RasterMeta)
}