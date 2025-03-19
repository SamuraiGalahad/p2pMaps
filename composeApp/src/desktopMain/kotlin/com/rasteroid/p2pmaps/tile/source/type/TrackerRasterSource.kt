package com.rasteroid.p2pmaps.tile.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.*
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.server.dto.TrackerRequestPeers
import com.rasteroid.p2pmaps.tile.RasterFormat
import com.rasteroid.p2pmaps.tile.RasterMeta
import com.rasteroid.p2pmaps.tile.TileMeta
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.DatagramSocket
import java.net.InetAddress

private val log = Logger.withTag("tracker raster source")

class TrackerRasterSource(
    private val remoteUrl: String,
    private val peerDiscoveryUrl: String,
    private val peerDiscoveryPort: Int
) : RasterSource(
    "Tracker",
    RasterSourceType.PEER
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json( Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun refresh() {
        announce()
        _rasters.value = requestRasters()
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

    private suspend fun requestRasters(): List<RasterMeta> {
        // Request available layers + tileMatrixSets.
        return client
            .get("$remoteUrl/maps")
            .body()
    }

    override suspend fun download(
        rasterMeta: RasterMeta,
        progressReport: (Int, Int) -> Unit
    ) {
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
                            RasterFormat.fromMime(tile.format)!!
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
    }
}