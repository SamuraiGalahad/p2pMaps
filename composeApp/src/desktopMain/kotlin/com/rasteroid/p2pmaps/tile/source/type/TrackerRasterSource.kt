package com.rasteroid.p2pmaps.tile.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.p2p.*
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.server.dto.TrackerAnnouncePeerInfo
import com.rasteroid.p2pmaps.server.dto.TrackerAskReply
import com.rasteroid.p2pmaps.server.dto.TrackerCheckReply
import com.rasteroid.p2pmaps.tile.LayerTMS
import com.rasteroid.p2pmaps.tile.TileFormat
import com.rasteroid.p2pmaps.tile.TileMeta
import com.rasteroid.p2pmaps.tile.getInternetSpeedTest
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

private const val ANNOUNCE_PERIOD = 60_000L // 60 seconds
private const val PEER_DISCOVERY_PERIOD = 10_000L // 10 seconds

class TrackerRasterSource(
    val remoteUrl: String
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TrackerRasterSource) return false
        if (remoteUrl != other.remoteUrl) return false
        return true
    }

    override suspend fun getRasters(): Result<List<LayerTMS>> {
        // We basically request /GetCapabilities from the tracker
        // and parse layers and TileMatrixSets from the response.
        // THe simple way is to just treat the XML response as plain text
        // and extract combinations of layers and TMSs that is has.
        try {
            val result = client.get("$remoteUrl/layers/GetCapabilities")
                .body<String>()

            val layers = mutableListOf<LayerTMS>()
            var parsingLayer = false
            var layerId = ""
            // Just parse the document line by line.
            // We assume the document is divided into lines.
            for (rawLine in result.lines()) {
                // Check if the line contains a layer and TMS.
                val line = rawLine.trim()

                if (line.startsWith("<Layer>")) {
                    parsingLayer = true
                    continue
                }

                if (line.startsWith("</Layer>")) {
                    parsingLayer = false
                    layerId = ""
                    continue
                }

                if (parsingLayer) {
                    // Check if the line contains a layer and TMS.
                    if (layerId.isEmpty() && line.startsWith("<ows:Identifier>")) {
                        layerId = line.substringAfter("<ows:Identifier>").substringBefore("</ows:Identifier>")
                    } else if (line.startsWith("<TileMatrixSet>")) {
                        val tmsId = line.substringAfter("<TileMatrixSet>").substringBefore("</TileMatrixSet>")
                        layers.add(LayerTMS(layerId, tmsId))
                    }
                }
            }

            return Result.success(layers)
        } catch (e: Exception) {
            log.e("Failed to get layers from tracker: ${e.message}")
            return Result.failure(e)
        }
    }

    override fun startBackground(scope: CoroutineScope) {
        scope.launch {
            while (scope.isActive) {
                try {
                    announceTMSs()
                } catch (e: Exception) {
                    log.e("Failed to announce TMSs to tracker")
                }
                delay(ANNOUNCE_PERIOD)
            }
        }
        scope.launch {
            while (scope.isActive) {
                try {
                    announceLayers()
                } catch (e: Exception) {
                    log.e("Failed to announce layers to tracker")
                }
                delay(ANNOUNCE_PERIOD)
            }
        }
        scope.launch {
            while (scope.isActive) {
                try {
                    announcePeerInfo()
                } catch (e: Exception) {
                    log.e("Failed to announce peer info to tracker")
                }
                delay(ANNOUNCE_PERIOD)
            }
        }
        scope.launch {
            while (scope.isActive) {
                try {
                    checkForPeerRequests(scope)
                    isAlive = true
                } catch (e: Exception) {
                    isAlive = false
                    log.e("Failed to check for peer requests: ${e.message}")
                }
                delay(PEER_DISCOVERY_PERIOD)
            }
        }
    }

    private suspend fun announceTMSs() {
        val rawTMSs = TileRepository.instance.getAllTMSMetaRaw()
        log.d("Announcing ${rawTMSs.size} TMSs to tracker")

        for (rawTMS in rawTMSs) {
            client.post("$remoteUrl/announce/tms") {
                url {
                    parameter("peerid", Settings.PEER_ID)
                }
                setBody(rawTMS)
            }
            // A bit of a delay to not spam the tracker.
            delay(100)
        }
    }

    private suspend fun announceLayers() {
        val rawLayers = TileRepository.instance.getAllLayersTMSLink()
        log.d("Announcing ${rawLayers.size} layers to tracker")

        for (rawLayer in rawLayers) {
            client.post("$remoteUrl/announce/layer") {
                url {
                    parameter("peerid", Settings.PEER_ID)
                }
                setBody(rawLayer)
            }
            // A bit of a delay to not spam the tracker.
            delay(100)
        }
    }

    private suspend fun announcePeerInfo() {
        // Clean up old info about connected peers.
        connectedPeers.entries.removeIf { entry ->
            System.currentTimeMillis() - entry.value.lastTimeSeenMillis > 60_000L
        }

        log.d("Testing internet connection")
        val internetSpeedTestResult = getInternetSpeedTest()

        log.d("Announcing peer info to tracker")
        client.post("$remoteUrl/announce/peer") {
            setBody {
                TrackerAnnouncePeerInfo(
                    peerId = Settings.PEER_ID,
                    connectedPeers = connectedPeers.map { it.value.peerId },
                    internetDownloadSpeed = 0L,
                    internetUploadSpeed = 0L
                )
            }
        }
    }

    private suspend fun

    private suspend fun checkForPeerRequests(scope: CoroutineScope) {
        log.d("Checking for requests from other peers")
        val response = client.get("$remoteUrl/peers/check") {
            parameter("peerid", Settings.PEER_ID)
            contentType(ContentType.Application.Json)
        }

        if (response.status.value == 204) {
            log.d("No requests from other peers")
            return
        }

        val result = response.body<TrackerCheckReply>()

        scope.launch {
            val punchResult = udpHolePunch(
                result.key,
                result.host,
                result.port
            )
            if (punchResult.isFailure) {
                log.e("Failed to hole punch to peer")
                return@launch
            }

            log.d("Successfully hole punched to peer")
            val (socket, _, _) = punchResult.getOrThrow()
            listen(socket)
        }
    }

    private suspend fun askForPeers(
        layer: String,
        tileMatrixSet: String
    ): List<TrackerAskReply> {
        return client
            .get("$remoteUrl/peers/ask") {
                url {
                    parameter("peerid", Settings.PEER_ID)
                    parameter("layer", layer)
                    parameter("tms", tileMatrixSet)
                }
            }.body<List<TrackerAskReply>>()
    }

    override suspend fun download(
        layerTMS: LayerTMS,
        progressReport: (Int, Int) -> Unit
    ) {
        // Request peers for this raster.
        val askedPeers = askForPeers(layerTMS.layer, layerTMS.tileMatrixSet)
        log.d("before udp hole punch")

        // TODO: For now assume only working with one peer.
        val peerConnection = askedPeers[0]

        // Hole punch to the peer.
        val (socket, address, port) = udpHolePunch(
            peerConnection.key,
            peerConnection.host,
            peerConnection.port
        ).getOrElse {
            log.e("Failed to hole punch to peer, aborting download")
            return
        }

        // Exchange peers ids.
        // We actually don't need to save the other peer's id,
        // but the other peer needs it.
        val peerIdResult = exchangePeerIds(socket, address, port)
        if (peerIdResult.isFailure) {
            log.e("Failed to exchange peer ids, aborting download")
            return
        }

        // Request tile matrix set info from peer.
        // We need it both to:
        // - Loop through tiles to request them
        // - Save tile matrix set info locally
        val tmsMeta = requestTileMatrixSet(socket, address, port, layerTMS.tileMatrixSet)
            .getOrElse {
                log.e("Failed to get tile matrix set from peer, aborting download")
                return
            }.tileMatrixSet

        if (tmsMeta == null) {
            log.e("Failed to get tile matrix set from peer, aborting download")
            return
        }

        // Loop through all tiles in the tile matrix set and request them.
        val totalTiles = tmsMeta.tileMatrixes.sumOf { it.matrixHeight * it.matrixWidth }
        var tilesTransferred = 0
        for (tileMatrix in tmsMeta.tileMatrixes) {
            for (tileCol in 0 until tileMatrix.matrixHeight) {
                for (tileRow in 0 until tileMatrix.matrixWidth) {
                    // Request tile from peer.
                    val tileMeta = TileMeta(
                        layer = layerTMS.layer,
                        tileMatrixSet = layerTMS.tileMatrixSet,
                        tileMatrix = tileMatrix.identifier,
                        tileRow = tileRow,
                        tileCol = tileCol,
                        format = TileFormat.PNG // TODO: For now, hardcoding .png as format.
                    )

                    val bytes = requestTile(
                        socket,
                        address,
                        port,
                        tileMeta
                    ).getOrElse {
                        log.e("Failed to get tile from peer, aborting download")
                        return
                    }

                    log.d("Received tile, size: ${bytes.size} bytes")

                    // Save the tile to disk.
                    TileRepository.instance.saveTile(
                        tileMeta,
                        bytes
                    )

                    // Report progress.
                    ++tilesTransferred
                    progressReport(tilesTransferred, totalTiles)

                    // A bit of a delay to not overwhelm the peer.
                    delay(10)
                }
            }
        }

        TileRepository.instance.saveTMSMeta(
            layerTMS.tileMatrixSet,
            tmsMeta
        )

        // Request layer info.
        val layerMetaRequestResult = requestLayer(socket, address, port, layerTMS.layer)
        if (layerMetaRequestResult.isFailure) {
            log.e("Failed to get layer from peer, aborting download")
            return
        }

        val layerMetaResult = layerMetaRequestResult.getOrThrow()
        val raster = layerMetaResult.raster
        if (raster == null) {
            log.e("Failed to get layer from peer, aborting download")
            return
        }

        TileRepository.instance.saveLayerMeta(layerTMS.layer, raster)

        // Send closing message.
        send(socket, address, port, Message.Close)

        log.d("Successfully downloaded layer: ${layerTMS.layer}, tms: ${layerTMS.tileMatrixSet}")
    }

    override fun hashCode(): Int {
        // Include remoteUrl in hash code.
        return remoteUrl.hashCode()
    }
}