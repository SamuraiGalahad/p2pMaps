package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.tile.SourcedLayerTMS
import com.rasteroid.p2pmaps.vm.ExternalRastersViewModel

@Composable
fun BrowseRastersScreen(
    viewModel: ExternalRastersViewModel
) {
    val rasters by viewModel.rasters.collectAsState()
    if (rasters.isEmpty()) {
        NoRastersToDownloadScreen()
    } else {
        RasterGridScreen(viewModel, rasters)
    }
}

@Composable
fun NoRastersToDownloadScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color.DarkGray,
        ) {
            Text("No rasters available to download")
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Add raster sources in")
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
fun RasterGridScreen(
    viewModel: ExternalRastersViewModel,
    rasters: List<SourcedLayerTMS>
) {
    // A simple 2-column grid
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(rasters) { sourcedRasterMeta ->
            DownloadRasterCard(sourcedRasterMeta) {
                viewModel.onDownloadSource(it)
            }
        }
    }
}

@Composable
fun DownloadRasterCard(
    sourcedLayerTMS: SourcedLayerTMS,
    onDownloadClick: (SourcedLayerTMS) -> Unit
) {
    RasterCard(
        sourcedLayerTMS.layerTMS.layer,
        sourcedLayerTMS.layerTMS.tileMatrixSet
    ) {
        Text(
            "Source: ${sourcedLayerTMS.source.name}"
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = MaterialTheme.shapes.small,
            enabled = sourcedLayerTMS.source.isAlive
                    && !TileRepository.instance.layers.contains(sourcedLayerTMS.layerTMS),
            contentPadding = PaddingValues(0.dp),
            onClick = { onDownloadClick(sourcedLayerTMS) }
        ) {
            Text(
                "Download",
                //style = MaterialTheme.typography.subtitle1,
                //color = MaterialTheme.colors.onPrimary.copy(alpha = 0.75f)
            )
        }
    }
}