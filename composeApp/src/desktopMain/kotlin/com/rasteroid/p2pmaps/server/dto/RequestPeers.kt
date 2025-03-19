package com.rasteroid.p2pmaps.server.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackerRequestPeers(
    @SerialName("LAYER_NAME")
    val layerName: String,
    @SerialName("MATRIX")
    val matrixes: List<TrackerConnectableMatrix>
)

@Serializable
data class TrackerConnectableMatrix(
    @SerialName("connection_key")
    val connectionKey: String,
    @SerialName("tile_matrix")
    val matrix: MutableMap<String, MutableList<TrackerTile>> = mutableMapOf()
)