package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.config.ensureFileExists
import com.rasteroid.p2pmaps.raster.ExternalRasterRepository
import com.rasteroid.p2pmaps.raster.source.type.PersistentPeerRasterSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val log = Logger.withTag("PeerRepository")

class PeerRepository(
    private val peersFilepath: Path,
) {
    companion object {
        val instance = PeerRepository(
            peersFilepath = Settings.APP_DATA_PATH.resolve("tmp_peers.json")
        )
    }

    data class SourcedPeerAddr(
        val isPersistent: Boolean,
        val peerAddr: PeerAddr
    )

    @Serializable
    data class PeerAddrCollection(
        val peers: List<PeerAddr> = emptyList()
    )

    private val peers = mutableSetOf<SourcedPeerAddr>()

    val persistentPeers: List<PeerAddr>
        get() = peers.filter { it.isPersistent }.map { it.peerAddr }

    init {
        ensureFileExists(peersFilepath)

        Settings.APP_CONFIG.peers.forEach {
            addPersistentPeer(it, false)
        }

        runCatching {
            Json.decodeFromString<PeerAddrCollection>(peersFilepath.readText())
        }.getOrDefault(PeerAddrCollection()).peers.forEach {
            addPeer(it, false)
        }
    }

    fun addPeer(peer: PeerAddr, save: Boolean = true) {
        val result = peers.add(SourcedPeerAddr(false, peer))
        if (result && save) {
            savePeers()
        }
    }

    fun addPersistentPeer(peer: PeerAddr, save: Boolean = true) {
        val existingPeer = peers.find { it.peerAddr == peer }
        if (existingPeer != null) {
            if (!existingPeer.isPersistent) {
                peers.remove(existingPeer)
                peers.add(SourcedPeerAddr(true, peer))
                ExternalRasterRepository.instance.addSource(PersistentPeerRasterSource(peer.host, peer.port))
                if (save) {
                    savePeers()
                    savePersistentPeers()
                }
            }
        } else {
            peers.add(SourcedPeerAddr(true, peer))
            if (save) savePersistentPeers()
        }
    }

    fun removePeer(peer: PeerAddr) {
        peers.find { it.peerAddr == peer }?.let {
            peers.remove(it)
            if (it.isPersistent) {
                ExternalRasterRepository.instance.removeSource(PersistentPeerRasterSource(peer.host, peer.port))
                savePersistentPeers()
            } else {
                savePeers()
            }
        }
    }

    private fun savePeers() {
        log.i { "Saving temporary peers to $peersFilepath" }
        runCatching {
            val tempPeers = peers.filter { !it.isPersistent }
                .map { it.peerAddr }
            val peersJson = Json.encodeToString(tempPeers)
            peersFilepath.writeText(peersJson)
        }
    }

    private fun savePersistentPeers() {
        log.i("Saving persistent peers ${Settings.APP_CONFIG_FILE_PATH}")
        Settings.APP_CONFIG.peers = persistentPeers
        Settings.writeAppConfig()
    }
}