package com.rasteroid.p2pmaps.raster.meta

import kotlinx.serialization.Serializable
import java.nio.file.Path

@Serializable
data class RasterInfo(
    val path: Path,
    val fileSize: Long,
    val meta: RasterMeta
)
