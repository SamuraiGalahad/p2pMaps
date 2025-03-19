package com.rasteroid.p2pmaps.server.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackerAnnounce(
    val uuid: String,
    val layers: List<TrackerLayer>
)

@Serializable
data class TrackerLayer(
    val name: String,
    @SerialName("type")
    val tileMatrixSet: String,
    val matrix: MutableMap<String, MutableList<TrackerTile>> = mutableMapOf()
)

@Serializable
data class TrackerTile(
    @SerialName("n")
    val col: Int,
    @SerialName("m")
    val row: Int,
    @SerialName("type")
    val format: String
)