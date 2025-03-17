package com.rasteroid.p2pmaps.p2p

import com.rasteroid.p2pmaps.tile.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// The main idea is to make a stateless protocol, so that
// we wouldn't need to track any sessions/individual peer connections.
@Serializable
sealed class Message {
    // Request a list of available rasters (layer + tile matrix set).
    @Serializable
    @SerialName("message.rasters")
    data object Rasters : Message()

    // Request a specific tile.
    @Serializable
    @SerialName("message.tile")
    data class Tile(val meta: TileMeta, val offsetBytes: Int, val limitBytes: Int) : Message()

    // Request tile size in bytes.
    @Serializable
    @SerialName("message.tileSize")
    data class TileSize(val meta: TileMeta) : Message()

    // Request a description for a raster (layer + tile matrix set):
    // a list of available tile matrixes.
    @Serializable
    @SerialName("message.describe")
    data class Describe(val raster: RasterMeta) : Message()

    @Serializable
    @SerialName("message.rastersReply")
    data class RastersReply(val rasters: List<RasterReply>) : Message()

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
    @SerialName("message.tileSizeReply")
    data class TileSizeReply(val dataSizeBytes: Int) : Message()

    @Serializable
    @SerialName("message.describeReply")
    data class DescribeReply(val tileMatrixSet: TileMatrixSet?) : Message()
}