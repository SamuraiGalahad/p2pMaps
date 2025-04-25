package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.SharingStarted

@Composable
fun RasterCard(
    layer: String,
    tms: String,
    content: @Composable () -> Unit
) {
    // A simple reusable card that displays in centered in column
    // layer, tms and some provided content.

    Card(
        // Add max size to it
        modifier = Modifier.size(
            width = 300.dp,
            height = 200.dp
        )
    ) {
        Column {
            Text("Layer: $layer")
            Text("Tile Matrix Set: $tms")
            content()
        }
    }
}
