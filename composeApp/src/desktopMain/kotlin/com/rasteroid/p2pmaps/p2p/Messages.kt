package com.rasteroid.p2pmaps.p2p

import com.rasteroid.p2pmaps.tile.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The main idea is to make a stateless protocol, so that
// we wouldn't need to track any sessions/individual peer connections.
@Serializable
sealed class Message {
    @Serializable
    @SerialName("message.ping")
    // Keep-alive message between peers.
    data object Ping : Message()

    @Serializable
    @SerialName("message.pong")
    // Reply to keep-alive message.
    data object Pong : Message()

    @Serializable
    @SerialName("messages.close")
    data object Close : Message()

    @Serializable
    @SerialName("message.layers")
    // Request a list of available rasters (layer id + tile matrix set id).
    data object Layers : Message()

    @Serializable
    @SerialName("message.layersReply")
    data class LayersReply(val layers: List<LayerTMS>) : Message()

    @Serializable
    @SerialName("message.layer")
    // Request a fixed description for a layer.
    data class Layer(val layer: String) : Message()

    @Serializable
    @SerialName("message.layerReply")
    data class LayerReply(val raster: LayerMeta?) : Message()

    @Serializable
    @SerialName("message.tileMatrixSet")
    // Request a description for a tile matrix set.
    data class TileMatrixSet(val tileMatrixSet: String) : Message()

    @Serializable
    @SerialName("message.tileMatrixSetReply")
    data class TileMatrixSetReply(val tileMatrixSet: TMSMeta?) : Message()

    // Request tile size in bytes.
    @Serializable
    @SerialName("message.tileSize")
    data class TileSize(val meta: TileMeta) : Message()

    @Serializable
    @SerialName("message.tileSizeReply")
    data class TileSizeReply(val dataSizeBytes: Int) : Message()

    // Request a specific tile.
    @Serializable
    @SerialName("message.tile")
    data class Tile(val meta: TileMeta, val offsetBytes: Int, val limitBytes: Int) : Message()

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
}