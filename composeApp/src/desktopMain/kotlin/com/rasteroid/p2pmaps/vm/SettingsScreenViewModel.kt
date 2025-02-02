package com.rasteroid.p2pmaps.vm

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.p2p.PeerAddr
import com.rasteroid.p2pmaps.p2p.PeerRepository

class SettingsScreenViewModel : ViewModel() {
    var addedPeers by mutableStateOf(PeerRepository.instance.persistentPeers)
    var newPeerInput by mutableStateOf("")
    var peersScrollState by mutableStateOf(ScrollState(0))

    fun addPeer(peer: PeerAddr) {
        PeerRepository.instance.addPersistentPeer(peer)
        addedPeers = PeerRepository.instance.persistentPeers
    }

    fun removePeer(peer: PeerAddr) {
        PeerRepository.instance.removePeer(peer)
        addedPeers = PeerRepository.instance.persistentPeers
    }
}