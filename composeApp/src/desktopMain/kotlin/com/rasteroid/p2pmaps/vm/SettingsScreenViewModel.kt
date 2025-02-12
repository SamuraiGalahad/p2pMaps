package com.rasteroid.p2pmaps.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.p2p.PeerAddr
import com.rasteroid.p2pmaps.p2p.PeerRepository

class SettingsScreenViewModel : ViewModel() {
    var addedPeers by mutableStateOf(PeerRepository.instance.persistentPeers)
    var listenerPort by mutableStateOf(Settings.APP_CONFIG.listenerPort.toString())
    var listenerPortIsError by mutableStateOf(false)
    var newPeerInput by mutableStateOf("")

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

    fun addPeer(peer: PeerAddr) {
        PeerRepository.instance.addPersistentPeer(peer)
        addedPeers = PeerRepository.instance.persistentPeers
    }

    fun removePeer(peer: PeerAddr) {
        PeerRepository.instance.removePeer(peer)
        addedPeers = PeerRepository.instance.persistentPeers
    }
}