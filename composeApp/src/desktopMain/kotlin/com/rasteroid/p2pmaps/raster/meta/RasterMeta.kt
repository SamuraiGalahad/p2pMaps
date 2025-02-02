package com.rasteroid.p2pmaps.raster.meta

import kotlinx.serialization.Serializable

@Serializable
data class BoundingBox(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
)

@Serializable
data class RasterMeta(
    val format: String,
    val width: Int,
    val height: Int,
    val layers: List<String>,
    val time: String, // TODO: Maybe we can store it better using some time/date object.
    val boundingBox: BoundingBox
)
