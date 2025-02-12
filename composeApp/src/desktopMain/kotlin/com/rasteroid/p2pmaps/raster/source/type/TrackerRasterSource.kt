package com.rasteroid.p2pmaps.raster.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import com.rasteroid.p2pmaps.raster.meta.RasterSourceMetaReply
import java.io.OutputStream

private val log = Logger.withTag("tracker raster source")

class TrackerRasterSource(
    private val remoteUrl: String
) : RasterSource {
    override val name: String = "Tracker"
    override val type: RasterSourceType = RasterSourceType.PEER

    override fun fetch(onRasterFound: (RasterSourceMetaReply) -> Unit) {
        log.d("Not implemented")
    }

    override fun download(resultStream: OutputStream, raster: RasterMeta) {
        log.d("Not implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackerRasterSource

        if (remoteUrl != other.remoteUrl) return false
        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = remoteUrl.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}