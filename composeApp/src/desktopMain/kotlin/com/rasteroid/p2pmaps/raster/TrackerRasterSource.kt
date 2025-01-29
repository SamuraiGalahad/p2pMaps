package com.rasteroid.p2pmaps.raster

class TrackerRasterSource : RasterSource {
    override val name: String = "Tracker"
    // Even though it's a tracker source, we're still downloading the rasters from peers.
    override val type: RasterSourceType = RasterSourceType.PEER

    override suspend fun fetchRasters(): List<RasterMeta> {
        // TODO
        return emptyList()
    }

    override suspend fun downloadRaster(raster: RasterMeta) {

    }
}