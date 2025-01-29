package com.rasteroid.p2pmaps.raster

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.PeerAddr
import com.rasteroid.p2pmaps.p2p.requestRaster
import com.rasteroid.p2pmaps.settings.Settings.APP_CONFIG_PATH
import com.rasteroid.p2pmaps.settings.ensureFileExists
import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addPathSource

private val log = Logger.withTag("saved peers raster source")

// Sort of a fallback raster source when tracker isn't available.
// The idea is that we can still query peers that didn't have their addresses changed
// after we saved them to a file.
class SavedPeersRasterSource : RasterSource {
    private val savedPeersPath = APP_CONFIG_PATH.resolve("peers.toml")
    private var peers: List<PeerAddr> = mutableListOf()

    init {
        log.d("Ensuring that saved peers file exists: $savedPeersPath")
        ensureFileExists(savedPeersPath)
        val savedPeersConfig = ConfigLoaderBuilder.default()
            .addPathSource(
                savedPeersPath,
                optional = true,
                allowEmpty = true
            ).build()
            .loadConfigOrThrow<SavedPeersConfig>()

        for (peer in savedPeersConfig.peers) {
            val parts = peer.split(":")
            if (parts.size != 2) {
                log.w("Ignoring invalid peer address: '$peer' (expected format: host:port)")
                continue
            }

            val (host, port) = parts
            if (port.toIntOrNull() == null) {
                log.w("Ignoring invalid peer address: '$peer' (port must be a number)")
                continue
            }

            peers += PeerAddr(host, port.toInt())
        }
    }

    override val name: String = "Saved Peers"
    override val type: RasterSourceType = RasterSourceType.PEER

    override suspend fun fetchRasters(): List<RasterMeta> {
        // There's no way to ask peers what rasters they have.
        // TODO: Maybe think about some other architecture for this.
        // Maybe split fetchRasters into some Fetchable interface?
        return emptyList()
    }

    override suspend fun downloadRaster(raster: RasterMeta) {
        // Iterate over all peers and try to request raster.
        // Stop on the first peer that doesn't return failure.
        log.i("Requesting raster $raster from saved peers")
        for (peer in peers) {
            log.d("Requesting raster $raster from $peer")
            val result = requestRaster(raster, peer.host, peer.port,
                onDataStart = { log.d("Started receiving raster $raster from $peer") },
                onDataReceived = { log.d("Received ${it.size} bytes of raster $raster from $peer") }
            )
            if (result.isSuccess) {
                log.i("Successfully received raster $raster from $peer")
                return
            }
            log.w("Failed to receive raster $raster from $peer", result.exceptionOrNull())
        }
    }
}