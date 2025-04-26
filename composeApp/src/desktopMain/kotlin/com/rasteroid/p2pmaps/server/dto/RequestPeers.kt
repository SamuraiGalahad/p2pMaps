package com.rasteroid.p2pmaps.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackerPeerConnection(
    val host: String,
    val port: Int,
    val key: String
)
