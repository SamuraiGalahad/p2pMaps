package com.rasteroid.p2pmaps.tile.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.*
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.tile.LayerTMS
import com.rasteroid.p2pmaps.tile.TileFormat
import com.rasteroid.p2pmaps.tile.TileMeta
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
        requestLayersTMS(
            socket,
            withContext(Dispatchers.IO) {
                InetAddress.getByName(host)
            },
            port
        ).onSuccess { reply ->
            log.d("Received ${reply.layers.size} rasters from peer")
            _rasters.value = reply.layers
        }.onFailure {
            log.e("Failed to get rasters from peer: $it")
        }
    }

    override suspend fun download(
        layerTMS: LayerTMS,
        progressReport: (Int, Int) -> Unit
    ) {
        // If we don't have needed TMS, request it.
        if (TileRepository.instance.getTMSMeta(layerTMS.tileMatrixSet) == null) {
            log.d("Requesting TMS ${layerTMS.tileMatrixSet} from peer")
            requestTileMatrixSet(
                socket,
                withContext(Dispatchers.IO) {
                    InetAddress.getByName(host)
                },
                port,
                layerTMS.tileMatrixSet
            ).onSuccess { reply ->
                log.d("Received TMS ${layerTMS.tileMatrixSet} from peer")
                TileRepository.instance.saveTMSMeta(layerTMS.tileMatrixSet, reply.tileMatrixSet!!)
            }.onFailure {
                log.e("Failed to get TMS ${layerTMS.tileMatrixSet} from peer: $it")
            }
        }

        // Now we should have TMS.
        val tms = TileRepository.instance.getTMSMeta(layerTMS.tileMatrixSet)
            ?: throw IllegalStateException("TMS ${layerTMS.tileMatrixSet} not found")

        val totalTiles = tms.tileMatrixes.sumOf { tileMatrix ->
            tileMatrix.matrixWidth * tileMatrix.matrixHeight
        }
        var handledTiles = 0

        // Download tiles, according to TMS.
        for (tileMatrix in tms.tileMatrixes) {
            for (tileRow in 0 until tileMatrix.matrixWidth) {
                for (tileCol in 0 until tileMatrix.matrixHeight) {
                    val tileMeta = TileMeta(
                        layerTMS.layer,
                        layerTMS.tileMatrixSet,
                        tileMatrix.identifier,
                        tileCol,
                        tileRow,
                        // TODO: For now hardcoding png.
                        TileFormat.PNG
                    )

                    // Check if we already have the tile.
                    val tile = TileRepository.instance.getTile(
                        tileMeta.layer,
                        tileMeta.tileMatrixSet,
                        tileMeta.tileMatrix,
                        tileMeta.tileCol,
                        tileMeta.tileRow,
                        tileMeta.format,
                        offsetBytes = 0,
                        limitBytes = Int.MAX_VALUE
                    )

                    if (tile == null) {
                        log.d("Requesting tile $handledTiles from peer")
                        // Request tile from peer.
                        requestTile(
                            socket,
                            withContext(Dispatchers.IO) {
                                InetAddress.getByName(host)
                            },
                            port,
                            tileMeta
                        ).onSuccess { reply ->
                            log.d("Received tile $handledTiles from peer")
                            // Save tile to repository.
                            TileRepository.instance.saveTile(tileMeta, reply)
                        }.onFailure {
                            log.e("Failed to get tile $handledTiles from peer: $it")
                        }
                    }

                    handledTiles++
                    progressReport(handledTiles, totalTiles)
                }
            }
        }
    }
}