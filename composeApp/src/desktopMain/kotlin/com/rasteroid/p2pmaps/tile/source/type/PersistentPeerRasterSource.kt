package com.rasteroid.p2pmaps.tile.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.getSocketOnAnyAvailablePort
import com.rasteroid.p2pmaps.p2p.requestRasters
import com.rasteroid.p2pmaps.tile.RasterMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress

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
    // Open datagram socket on random port.
    private val socket = getSocketOnAnyAvailablePort()

    override suspend fun refresh() {
        requestRasters(
            socket,
            withContext(Dispatchers.IO) {
                InetAddress.getByName(host)
            },
            port
        ).onSuccess { reply ->
            log.d("Received ${reply.rasters.size} rasters from peer")
            val newRasters = mutableListOf<RasterMeta>()
            // Individually add each tileMatrixSet combination.
            for (raster in reply.rasters) {
                for (tileMatrixSet in raster.tileMatrixSetLinks) {
                    newRasters.add(RasterMeta(raster.identifier, tileMatrixSet.tileMatrixSet))
                }
            }
            // Update _rasters.
            _rasters.value = newRasters
        }.onFailure {
            log.e("Failed to get rasters from peer: $it")
        }
    }

    override suspend fun download(
        rasterMeta: RasterMeta,
        progressReport: (Int, Int) -> Unit
    ) {
        log.d("TODO")
    }
}