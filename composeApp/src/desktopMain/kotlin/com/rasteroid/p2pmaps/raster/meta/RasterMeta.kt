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

// Essentially a copy of RasterMeta, but every field may be nullable.
// Nullable fields are used to indicate that the field is not given by the source
// and should be filled in by the client.
@Serializable
data class RasterSourceMetaReply(
    var format: String? = null,
    var width: Int? = null,
    var height: Int? = null,
    var layers: List<String>? = null,
    var time: String? = null,
    var boundingBox: BoundingBox? = null
)