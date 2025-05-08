package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.server.ProgressLayerTMS
import com.rasteroid.p2pmaps.vm.InternalRastersViewModel
import kotlin.math.floor

@Composable
fun InternalRastersScreen(
    viewModel: InternalRastersViewModel
) {
    if (viewModel.rasters.isEmpty()) {
        NoInternalRastersFound()
        return
    }
    RastersGrid(viewModel.rasters)
}

@Composable
fun NoInternalRastersFound() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color.DarkGray,
        ) {
            Text("No rasters downloaded yet")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Browse available rasters in")
                Icon(Icons.Filled.ImageSearch, contentDescription = "Browser")
            }
        }
    }
}

@Composable
fun RastersGrid(
    rasters: List<ProgressLayerTMS>,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Create a grid of fixed size cards, going from left to right, top to bottom.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 300.dp),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rasters) { progressRaster ->
                    ProgressRasterCard(progressRaster)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            val wmtsUrl = "localhost:${Settings.APP_CONFIG.localWMTSServerPort}/wmts"
                Text(
                    text = "Tiles are served via WMTS at ",
                )
            SelectionContainer {
                Text(
                    text = wmtsUrl,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun ProgressRasterCard(
    progressRaster: ProgressLayerTMS
) {
    val layerTMS = progressRaster.layerTMS
    val progress = progressRaster.current / progressRaster.total.toFloat()
    val progressPercentage = floor(progress * 100)

    RasterCard(
        layer = layerTMS.layer,
        tms = layerTMS.tileMatrixSet
    ) {
        Spacer(modifier = Modifier.size(16.dp))
        Box(
            // fill max width, but have a bit of an indent from the sides
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            LinearProgressIndicator(
                progress,
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(6.dp))
                    .progressSemantics(progress)
            )

            Text(
                text = "${progressRaster.current}/${progressRaster.total} tiles ($progressPercentage%)",
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.onPrimary.copy(alpha = 0.75f)
            )
        }
    }
}