package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.p2p.PeerAddr
import com.rasteroid.p2pmaps.p2p.PeerRepository
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

    Column {
        Text("Peer-to-peer", style = MaterialTheme.typography.h6)
        Spacer(modifier = Modifier.height(8.dp))
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
                items(viewModel.addedPeers) { peer ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display the peer info.
                        Text(
                            text = peer.toString(),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            viewModel.removePeer(peer)
                        }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove Peer")
                        }
                    }
                }
            }
        }
    }
}