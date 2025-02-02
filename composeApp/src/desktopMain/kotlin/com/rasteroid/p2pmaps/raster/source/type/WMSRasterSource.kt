package com.rasteroid.p2pmaps.raster.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import java.io.OutputStream

private val log = Logger.withTag("WMS raster source")

class WMSRasterSource : RasterSource {
    override val name: String = "WMS"
    override val type: RasterSourceType = RasterSourceType.EXTERNAL_WMS

    override fun fetch(onRasterFound: (RasterMeta) -> Unit) {

    }

    override fun download(resultStream: OutputStream, raster: RasterMeta) {
        log.d("Not implemented")
    }
}