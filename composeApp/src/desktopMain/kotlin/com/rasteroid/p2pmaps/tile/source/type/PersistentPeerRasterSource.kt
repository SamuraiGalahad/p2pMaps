package com.rasteroid.p2pmaps.tile.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.RasterMeta

private val log = Logger.withTag("persistent peer raster source")

// Sort of a fallback raster source when tracker isn't available.
// The idea is that we can still query peers that didn't have their addresses changed
// after we saved them to a file.
class PersistentPeerRasterSource(
    private val host: String,
    private val port: Int
) : RasterSource(
    "Saved Peers",
    RasterSourceType.PEER
) {
    override suspend fun refresh() {
        log.d("TODO")
    }

    override suspend fun download(
        rasterMeta: RasterMeta,
        progressReport: (Int, Int) -> Unit
    ) {
        log.d("TODO")
    }
}