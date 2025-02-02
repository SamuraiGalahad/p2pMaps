package com.rasteroid.p2pmaps.config

import com.rasteroid.p2pmaps.p2p.PeerAddr
import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    var peers: List<PeerAddr> = listOf()

)
