package com.rasteroid.p2pmaps.server.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackerAnnouncePeerInfo(
    @SerialName("peerid")
    val peerId: String,
    // Connected peer ids.
    val connectedPeers: List<String>,
    // Speed in bit/s.
    val internetDownloadSpeed: Long,
    // Speed in bit/s.
    val internetUploadSpeed: Long,
)