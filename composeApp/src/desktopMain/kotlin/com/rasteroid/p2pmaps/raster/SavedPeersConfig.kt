package com.rasteroid.p2pmaps.raster

import kotlinx.serialization.Serializable

typealias PeerHostPort = String // "host:port", like "12.34.56.78:4242"

@Serializable
data class SavedPeersConfig(
    val peers: List<PeerHostPort> = emptyList()
)
