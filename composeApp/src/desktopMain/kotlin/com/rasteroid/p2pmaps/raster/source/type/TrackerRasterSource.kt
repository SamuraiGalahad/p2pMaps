package com.rasteroid.p2pmaps.raster.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import java.io.OutputStream

private val log = Logger.withTag("tracker raster source")

class TrackerRasterSource(
    private val remoteUrl: String
) : RasterSource {
    override val name: String = "Tracker"
    override val type: RasterSourceType = RasterSourceType.PEER

    override fun fetch(onRasterFound: (RasterMeta) -> Unit) {
        log.d("Not implemented")
    }

    override fun download(resultStream: OutputStream, raster: RasterMeta) {
        log.d("Not implemented")
    }
}