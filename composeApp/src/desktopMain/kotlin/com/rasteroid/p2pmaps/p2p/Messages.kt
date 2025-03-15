package com.rasteroid.p2pmaps.p2p

import com.rasteroid.p2pmaps.tile.LayerMeta
import com.rasteroid.p2pmaps.tile.TileMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Message {
    @Serializable
    @SerialName("message.close")
    data object Close : Message()

    @Serializable
    @SerialName("message.ok")
    data object Ok : Message()

    @Serializable
    @SerialName("message.rasters")
    data object Rasters : Message()

    @Serializable
    @SerialName("message.tile")
    data class Tile(val meta: TileMeta) : Message()

    @Serializable
    @SerialName("message.want")
    data class Want(val layer: String) : Message()

    @Serializable
    @SerialName("message.query")
    data class Query(val tileMatrixSet: String) : Message()

    @Serializable
    @SerialName("message.tileReply")
    data class TileReply(val tile: ByteArray) : Message() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as TileReply
            return tile.contentEquals(other.tile)
        }
        override fun hashCode(): Int {
            return tile.contentHashCode()
        }
    }

    @Serializable
    @SerialName("message.wantReply")
    data class WantReply(val meta: LayerMeta) : Message()
}