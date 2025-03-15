package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rasteroid.p2pmaps.tile.meta.RasterSourceMetaReply

@Composable
fun BaseRasterCard(
    meta: RasterSourceMetaReply,
    bottomContent: @Composable() (ColumnScope.() -> Unit) = {}
) {
    val format = meta.format ?: "???"
    val width = meta.width ?: "???"
    val height = meta.height ?: "???"
    val time = meta.time ?: "Any time"
    val layers = meta.layers ?: listOf("???")
    val minX = meta.boundingBox?.minX ?: "???"
    val minY = meta.boundingBox?.minY ?: "???"
    val maxX = meta.boundingBox?.maxX ?: "???"
    val maxY = meta.boundingBox?.maxY ?: "???"

    Card(
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = format.uppercase(),
                    style = MaterialTheme.typography.h6.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(text = time, style = MaterialTheme.typography.subtitle2)
            }
            Spacer(Modifier.height(8.dp))

            Text(
                text = "$width x $height px",
                style = MaterialTheme.typography.body1
            )

            Spacer(Modifier.height(8.dp))

            val maxLayersToShow = 3
            val visibleLayers = layers.take(maxLayersToShow)
            LazyColumn(
                modifier = Modifier
                    .heightIn(min = 0.dp, max = 90.dp)
            ) {
                items(visibleLayers) { layer ->
                    Text(layer, style = MaterialTheme.typography.body2)
                }
                if (layers.size > maxLayersToShow) {
                    item {
                        Text("...", style = MaterialTheme.typography.body2)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Min X: $minX", style = MaterialTheme.typography.caption)
                    Text("Max X: $maxX", style = MaterialTheme.typography.caption)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Min Y: $minY", style = MaterialTheme.typography.caption)
                    Text("Max Y: $maxY", style = MaterialTheme.typography.caption)
                }
            }

            // Bottom content slot, e.g. download button or progress bar
            Spacer(Modifier.height(12.dp))
            Column {
                bottomContent()
            }
        }
    }
}