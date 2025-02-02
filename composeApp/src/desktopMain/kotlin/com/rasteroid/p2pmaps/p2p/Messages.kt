package com.rasteroid.p2pmaps.p2p

import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class Message {
    @Serializable
    @SerialName("message.close")
    data object Close : Message()

    @Serializable
    @SerialName("message.query")
    data object Query : Message()

    @Serializable
    @SerialName("message.metas")
    data class Metas(val metas: List<RasterMeta>) : Message()

    @Serializable
    @SerialName("message.reply")
    data class Reply(val reply: Boolean) : Message()

    @Serializable
    @SerialName("message.have")
    data class Have(val meta: RasterMeta) : Message()

    @Serializable
    @SerialName("message.want")
    data class Want(val meta: RasterMeta) : Message()

    @Serializable
    @SerialName("message.startData")
    data class StartData(val dataSizeBytes: Long) : Message()

    @Serializable
    @SerialName("message.data")
    data class Data(val data: ByteArray) : Message() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Data
            return data.contentEquals(other.data)
        }
        override fun hashCode() = data.contentHashCode()
    }
}