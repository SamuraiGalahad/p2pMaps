package com.rasteroid.p2pmaps.raster.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import com.rasteroid.p2pmaps.raster.meta.RasterSourceMetaReply
import java.io.OutputStream

private val log = Logger.withTag("WMS raster source")

class WMSRasterSource(
    private val remoteUrl: String
) : RasterSource {
    override val name: String = "WMS"
    override val type: RasterSourceType = RasterSourceType.EXTERNAL_WMS

    override fun fetch(onRasterFound: (RasterSourceMetaReply) -> Unit) {
        if (remoteUrl.isEmpty()) {
            log.w("Remote URL is empty, fetching disabled")
            return
        }
        
    }

    override fun download(resultStream: OutputStream, raster: RasterMeta) {
        if (remoteUrl.isEmpty()) {
            log.w("Remote URL is empty, downloading disabled")
            return
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as WMSRasterSource

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