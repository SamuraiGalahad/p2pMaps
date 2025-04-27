package com.rasteroid.p2pmaps.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.p2p.PeerAddr
import com.rasteroid.p2pmaps.p2p.PeerRepository
import com.rasteroid.p2pmaps.tile.ExternalRasterRepository
import com.rasteroid.p2pmaps.tile.source.type.TrackerRasterSource

class SettingsScreenViewModel : ViewModel() {
    val addedSources = ExternalRasterRepository.instance.sources

    var listenerPort by mutableStateOf(Settings.APP_CONFIG.listenerPort.toString())
    var listenerPortIsError by mutableStateOf(false)
    var newPeerInput by mutableStateOf("")

    var newTrackerInput by mutableStateOf("")

    var localWMTSServerPort by mutableStateOf(Settings.APP_CONFIG.localWMTSServerPort.toString())
    var localWMTSServerPortIsError by mutableStateOf(false)

    fun setListenerPortValue(port: String) {
        listenerPort = port
        val portInt = port.toIntOrNull()
        if (portInt != null && portInt in 0..65535) {
            Settings.APP_CONFIG.listenerPort = portInt
            Settings.writeAppConfig()
            listenerPortIsError = false
        } else {
            listenerPortIsError = true
        }
    }

    fun setLocalWMTSServerPortValue(port: String) {
        localWMTSServerPort = port
        val portInt = port.toIntOrNull()
        if (portInt != null && portInt in 0..65535) {
            Settings.APP_CONFIG.localWMTSServerPort = portInt
            Settings.writeAppConfig()
            localWMTSServerPortIsError = false
        } else {
            localWMTSServerPortIsError = true
        }
    }

    fun addPeer(peer: PeerAddr) {
        PeerRepository.instance.addPersistentPeer(peer)
    }

    fun removePeer(peer: PeerAddr) {
        PeerRepository.instance.removePeer(peer)
    }

    fun addTracker(tracker: String) {
        if (tracker in Settings.APP_CONFIG.trackerUrls) return
        ExternalRasterRepository.instance.addSource(TrackerRasterSource(tracker))
        Settings.APP_CONFIG.trackerUrls += tracker
        Settings.writeAppConfig()
        newTrackerInput = ""
    }

    fun removeTracker(tracker: String) {
        if (tracker in Settings.APP_CONFIG.trackerUrls) {
            ExternalRasterRepository.instance.removeSource(TrackerRasterSource(tracker))
            Settings.APP_CONFIG.trackerUrls -= tracker
            Settings.writeAppConfig()
        }
    }
}