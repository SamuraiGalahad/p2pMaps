package com.rasteroid.p2pmaps.p2p

import kotlinx.serialization.Serializable

@Serializable
data class PeerAddr(
    val host: String,
    val port: Int
) {
    override fun toString(): String {
        return "$host:$port"
    }
}
