package com.rasteroid.p2pmaps.p2p

import kotlinx.serialization.Serializable

@Serializable
data class PeerAddr(
    val host: String,
    val port: Int
) {
    companion object {
        fun fromString(str: String): Result<PeerAddr> {
            val parts = str.split(":")
            return if (parts.size == 2) {
                val host = parts[0]
                val port = parts[1].toIntOrNull()
                if (port != null) {
                    Result.success(PeerAddr(host, port))
                } else {
                    Result.failure(IllegalArgumentException("Invalid port number"))
                }
            } else {
                Result.failure(IllegalArgumentException("Invalid address format"))
            }
        }
    }

    override fun toString(): String {
        return "$host:$port"
    }
}
