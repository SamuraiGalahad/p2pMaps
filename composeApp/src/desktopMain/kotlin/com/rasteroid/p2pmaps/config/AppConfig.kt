package com.rasteroid.p2pmaps.config

import kotlinx.serialization.Serializable

@Serializable
data class AppConfig(
    var peers: List<String> = listOf(),
    var listenerPort: Int = 12345,
    var wmsRemoteUrl: String = "",
    var localWMTSServerPort: Int = 35267,
    var trackerUrls: List<String> = listOf()
)
