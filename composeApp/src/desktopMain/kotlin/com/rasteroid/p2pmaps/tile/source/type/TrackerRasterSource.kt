package com.rasteroid.p2pmaps.tile.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.tile.RasterMeta
import java.io.OutputStream

private val log = Logger.withTag("tracker raster source")

class TrackerRasterSource(
    private val remoteUrl: String
) : RasterSource {
    override val name: String = "Tracker"
    override val type: RasterSourceType = RasterSourceType.PEER

    init {
        val ktorClient = 
    }

    fun announce() {
        log.d("Announcing tracker")
        TileRepository.instance.getContents()
    }

    override fun fetch(onLayerFound: (RasterMeta) -> Unit) {
        //
    }

    override fun download(
        resultStream: OutputStream,
        rasterMeta: RasterMeta,
        onDataStart: (Long) -> Unit
    ) {
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