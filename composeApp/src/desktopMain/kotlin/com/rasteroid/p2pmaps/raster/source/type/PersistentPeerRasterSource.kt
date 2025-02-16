package com.rasteroid.p2pmaps.raster.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.PeerRepository
import com.rasteroid.p2pmaps.p2p.requestMetas
import com.rasteroid.p2pmaps.p2p.requestRaster
import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import com.rasteroid.p2pmaps.raster.meta.RasterSourceMetaReply
import java.io.OutputStream

private val log = Logger.withTag("persistent peer raster source")

// Sort of a fallback raster source when tracker isn't available.
// The idea is that we can still query peers that didn't have their addresses changed
// after we saved them to a file.
class PersistentPeerRasterSource(
    private val host: String,
    private val port: Int
) : RasterSource {
    override val name: String = "Saved Peers"
    override val type: RasterSourceType = RasterSourceType.PEER

    override fun fetch(onRasterFound: (RasterSourceMetaReply) -> Unit) {
        // Fetch rasters from all saved peers.
        for (peer in PeerRepository.instance.persistentPeers) {
            log.d("Requesting rasters from $peer")
            val result = requestMetas(peer.host, peer.port)
            if (result.isSuccess) {
                result.getOrThrow().forEach {
                    log.d("Received raster info $it from $peer")
                    onRasterFound(RasterSourceMetaReply(
                        format = it.format,
                        width = it.width,
                        height = it.height,
                        layers = it.layers,
                        time = it.time,
                        boundingBox = it.boundingBox
                    ))
                }
                log.i("Successfully received rasters from $peer")
            } else {
                log.w("Failed to receive rasters from $peer")
            }
        }
    }

    override fun download(
        resultStream: OutputStream,
        raster: RasterMeta,
        onDataStart: (Long) -> Unit
    ) {
        // Iterate over all peers and try to request raster.
        // Stop on the first peer that doesn't return failure.
        log.i("Requesting raster $raster from saved peers")
        for (peer in PeerRepository.instance.persistentPeers) {
            log.d("Requesting raster $raster from $peer")
            val result = requestRaster(
                raster, peer.host, peer.port,
                onDataStart = {
                    log.d("Started receiving raster $raster from $peer")
                    onDataStart(it)
                },
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersistentPeerRasterSource

        if (port != other.port) return false
        if (host != other.host) return false
        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = port
        result = 31 * result + host.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}