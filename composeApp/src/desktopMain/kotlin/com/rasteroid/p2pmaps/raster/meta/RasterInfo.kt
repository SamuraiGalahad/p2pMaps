package com.rasteroid.p2pmaps.raster.meta

import kotlinx.serialization.Serializable

@Serializable
data class RasterInfo(
    val path: String,
    val fileSize: Long,
    val meta: RasterMeta
)
