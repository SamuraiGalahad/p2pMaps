package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.PeerAddr
import com.rasteroid.p2pmaps.p2p.PeerRepository
import com.rasteroid.p2pmaps.tile.source.type.PersistentPeerRasterSource
import com.rasteroid.p2pmaps.tile.source.type.TrackerRasterSource
import com.rasteroid.p2pmaps.vm.SettingsScreenViewModel

private val log = Logger.withTag("settings screen")

@Composable
fun SettingsScreen(
    viewModel: SettingsScreenViewModel,
) {
    Column(modifier = Modifier.padding(8.dp)) {
        PeerSettings(viewModel)
    }
}

@Composable
fun PeerSettings(
    viewModel: SettingsScreenViewModel,
) {
    val addedSources by viewModel.addedSources.collectAsState()
    val scrollState = rememberScrollState()

    // Helper function to attempt adding a new peer.
    fun tryAddPeer() {
        // Example validation: expect input in "host:port" format.
        val parts = viewModel.newPeerInput.split(":")
        if (parts.size == 2) {
            val host = parts[0].trim()
            val port = parts[1].toIntOrNull()
            if (host.isNotEmpty() && port != null) {
                viewModel.addPeer(PeerAddr(host, port))
                viewModel.newPeerInput = "" // Clear the input field.
                log.d(PeerRepository.instance.persistentPeers.toString())
            } else {
                log.i("Invalid host:port format")
            }
        }
    }

    fun tryAddTracker() {
        val tracker = viewModel.newTrackerInput
        if (tracker.isNotEmpty()) {
            viewModel.addTracker(tracker)
            log.d("Added tracker: $tracker")
        } else {
            log.i("Invalid tracker URL")
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text("Peer-to-peer", style = MaterialTheme.typography.h6)
        Text("Port to listen on for local peers")
        TextField(
            value = viewModel.listenerPort,
            onValueChange = {
                viewModel.setListenerPortValue(it)
            },
            isError = viewModel.listenerPortIsError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("port") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Manually add peers")
        TextField(
            value = viewModel.newPeerInput,
            onValueChange = { viewModel.newPeerInput = it },
            placeholder = { Text("host:port") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { tryAddPeer() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Peer")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 150.dp)
        ) {
            LazyColumn {
                items(addedSources.filterIsInstance<PersistentPeerRasterSource>()) { peer ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display the peer info.
                        Text(
                            text = "${peer.host}:${peer.port}"
                        )
                        Spacer(Modifier.width(8.dp))
                        // Draw a red/green circle based on isAlive.
                        if (peer.isAlive) {
                            Icon(
                                Icons.Filled.Circle,
                                contentDescription = "Peer is alive",
                                tint = Color.Green.copy(alpha = 0.5f)
                            )
                        } else {
                            Icon(
                                Icons.Filled.Circle,
                                contentDescription = "Peer is not alive",
                                tint = Color.Red.copy(alpha = 0.5f)
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = {
                            viewModel.removePeer(PeerAddr(peer.host, peer.port))
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove Peer")
                        }
                    }
                }
            }
        }
        Text("Trackers", style = MaterialTheme.typography.h6)
        Text("Manually add trackers")
        TextField(
            value = viewModel.newTrackerInput,
            onValueChange = { viewModel.newTrackerInput = it },
            placeholder = { Text("tracker url") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = { tryAddTracker() }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Tracker")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Box(modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 150.dp)
        ) {
            LazyColumn {
                items(addedSources.filterIsInstance<TrackerRasterSource>()) { tracker ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display the tracker info.
                        Text(
                            text = tracker.remoteUrl,
                        )
                        Spacer(Modifier.width(8.dp))
                        // Draw a red/green circle based on isAlive.
                        if (tracker.isAlive) {
                            Icon(
                                Icons.Filled.Circle,
                                contentDescription = "Tracker is alive",
                                tint = Color.Green.copy(alpha = 0.5f),
                            )
                        } else {
                            Icon(
                                Icons.Filled.Circle,
                                contentDescription = "Tracker is not alive",
                                tint = Color.Red.copy(alpha = 0.5f),
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = {
                            viewModel.removeTracker(tracker.remoteUrl)
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove Tracker")
                        }
                    }
                }
            }
        }
        Text("Local WMTS server", style = MaterialTheme.typography.h6)
        Text("Port to listen on for local WMTS server (restart app to apply)")
        TextField(
            value = viewModel.localWMTSServerPort,
            onValueChange = {
                viewModel.setLocalWMTSServerPortValue(it)
            },
            isError = viewModel.localWMTSServerPortIsError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("port") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}