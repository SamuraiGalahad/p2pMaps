package com.rasteroid.p2pmaps.tile.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.server.dto.TrackerRequestPeers
import com.rasteroid.p2pmaps.tile.LayerTMS
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json


class TrackerRasterSource(
    private val remoteUrl: String,
    private val peerDiscoveryUrl: String,
    private val peerDiscoveryPort: Int
) : RasterSource(
    "Tracker",
    RasterSourceType.PEER
) {
    private val log = Logger.withTag("tracker $remoteUrl")
    private val client = HttpClient {
        install(ContentNegotiation) {
            json( Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun getRasters(): Result<List<LayerTMS>> {
        // Request $remote_url/maps from the server and wrap it in a Result.
        try {
            val rasters = client
                .get("$remoteUrl/maps")
                .body<List<LayerTMS>>()
            log.d("Received rasters from tracker: $rasters")
            return Result.success(rasters)
        } catch (e: Exception) {
            log.e("Failed to get rasters from tracker")
            return Result.failure(e)
        }
    }

    private suspend fun announce() {
        client.post("$remoteUrl/announce") {
            contentType(ContentType.Application.Json)
            setBody(TileRepository.instance.getAnnounce())
        }
    }

    private suspend fun requestPeers(layer: String, tileMatrixSet: String): TrackerRequestPeers {
        // Request peers to contact.
        return client
            .get("$remoteUrl/peers/$layer/$tileMatrixSet")
            .body()
    }

    override suspend fun download(
        rasterMeta: LayerTMS,
        progressReport: (Int, Int) -> Unit
    ) {
        // TODO.
        /*
        // Request peers for this raster.
        val peers = requestPeers(rasterMeta.layer, rasterMeta.tileMatrixSet)

        // Try punching a hole for all peers.
        val sockets = mutableListOf<Triple<String, DatagramSocket, PeerAddr>>()
        for (matrix in peers.matrixes) {
            val (socket, peerAddr) = udpHolePunch(
                matrix.connectionKey,
                peerDiscoveryUrl,
                peerDiscoveryPort
            )
            sockets.add(Triple(matrix.connectionKey, socket, peerAddr))
        }

        // Request tiles from peers.
        for ((connectionKey, socket, peerAddr) in sockets) {
            val scope = CoroutineScope(Dispatchers.Default)
            scope.launch {
                val connectableMatrix = peers.matrixes.find { it.connectionKey == connectionKey }!!
                for ((tileMatrix, tiles) in connectableMatrix.matrix) {
                    for (tile in tiles) {
                        val tileMeta = TileMeta(
                            rasterMeta,
                            tileMatrix,
                            tile.col,
                            tile.row,
                            TileFormat.fromMime(tile.format)!!
                        )
                        val result = requestTile(socket, InetAddress.getByName(peerAddr.host), peerAddr.port, tileMeta)
                        if (result.isSuccess) {
                            TileRepository.instance.saveTile(tileMeta, result.getOrThrow())
                        }
                    }
                }
            }
        }

        // Request layer info from any socket.
        val (_, socket, peerAddr) = sockets.first()
        val layerInfo = requestLayerInfo(socket, withContext(Dispatchers.IO) {
            InetAddress.getByName(peerAddr.host)
        }, peerAddr.port, rasterMeta.layer)

        // Save layer info.
        if (layerInfo.isSuccess) {
            TileRepository.instance.saveLayerInfo(rasterMeta.layer, layerInfo.getOrThrow().raster!!)
        }

        // Request tileMatrixSet info from any socket.
        val tileMatrixSetInfo = requestTileMatrixSetInfo(socket, withContext(Dispatchers.IO) {
            InetAddress.getByName(peerAddr.host)
        }, peerAddr.port, rasterMeta)

        // Save tileMatrixSet info.
        if (tileMatrixSetInfo.isSuccess) {
            TileRepository.instance.saveTileMatrixSetInfo(
                rasterMeta.layer,
                rasterMeta.tileMatrixSet,
                tileMatrixSetInfo.getOrThrow().tileMatrixSet!!
            )
        }
        */
    }
}