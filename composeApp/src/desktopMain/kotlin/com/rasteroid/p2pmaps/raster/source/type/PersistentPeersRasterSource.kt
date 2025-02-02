package com.rasteroid.p2pmaps.raster.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.PeerRepository
import com.rasteroid.p2pmaps.p2p.requestMetas
import com.rasteroid.p2pmaps.p2p.requestRaster
import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import java.io.OutputStream

private val log = Logger.withTag("persistent peers raster source")

// Sort of a fallback raster source when tracker isn't available.
// The idea is that we can still query peers that didn't have their addresses changed
// after we saved them to a file.
class PersistentPeersRasterSource : RasterSource {
    override val name: String = "Saved Peers"
    override val type: RasterSourceType = RasterSourceType.PEER

    override fun fetch(onRasterFound: (RasterMeta) -> Unit) {
        // Fetch rasters from all saved peers.
        for (peer in PeerRepository.instance.persistentPeers) {
            log.d("Requesting rasters from $peer")
            val result = requestMetas(peer.host, peer.port)
            if (result.isSuccess) {
                result.getOrThrow().forEach {
                    log.d("Received raster info $it from $peer")
                    onRasterFound(it)
                }
                log.i("Successfully received rasters from $peer")
            } else {
                log.w("Failed to receive rasters from $peer", result.exceptionOrNull())
            }
        }
    }

    override fun download(
        resultStream: OutputStream,
        raster: RasterMeta
    ) {
        // Iterate over all peers and try to request raster.
        // Stop on the first peer that doesn't return failure.
        log.i("Requesting raster $raster from saved peers")
        for (peer in PeerRepository.instance.persistentPeers) {
            log.d("Requesting raster $raster from $peer")
            val result = requestRaster(
                raster, peer.host, peer.port,
                onDataStart = { log.d("Started receiving raster $raster from $peer") },
                onDataReceived = {
                    log.d("Received ${it.size} bytes of raster $raster from $peer")
                    resultStream.write(it)
                }
            )
            if (result.isSuccess) {
                log.i("Successfully received raster $raster from $peer")
                return
            }
            log.w("Failed to receive raster $raster from $peer", result.exceptionOrNull())
        }
    }
}