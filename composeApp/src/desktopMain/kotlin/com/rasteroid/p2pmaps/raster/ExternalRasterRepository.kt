package com.rasteroid.p2pmaps.raster

import com.rasteroid.p2pmaps.raster.source.type.RasterSource
import com.rasteroid.p2pmaps.raster.source.type.PersistentPeersRasterSource
import com.rasteroid.p2pmaps.raster.source.type.TrackerRasterSource
import com.rasteroid.p2pmaps.raster.source.type.WMSRasterSource

class ExternalRasterRepository(
    val sources: List<RasterSource>
) {
    companion object {
        // Singleton for now, maybe DI later.
        val instance = ExternalRasterRepository(
            sources = listOf(
                PersistentPeersRasterSource(),
                TrackerRasterSource("localhost:12345"),
                WMSRasterSource()
            )
        )
    }
}