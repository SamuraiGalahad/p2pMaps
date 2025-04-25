package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.tile.SourcedLayerTMS
import com.rasteroid.p2pmaps.vm.ExternalRastersViewModel

@Composable
fun BrowseRastersScreen(
    viewModel: ExternalRastersViewModel
) {
    RasterGridScreen(viewModel)
}

@Composable
fun RasterGridScreen(viewModel: ExternalRastersViewModel) {
    val rasters by viewModel.rasters.collectAsState()
    // A simple 2-column grid
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(rasters) { sourcedRasterMeta ->
            RasterCard(sourcedRasterMeta) {
                viewModel.onDownloadSource(it)
            }
        }
    }
}

@Composable
fun RasterCard(
    sourcedLayerTMS: SourcedLayerTMS,
    onDownloadClick: (SourcedLayerTMS) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Layer: ${sourcedLayerTMS.layerTMS.layer}")
            Text(text = "TileMatrixSet: ${sourcedLayerTMS.layerTMS.tileMatrixSet}")
            Text(text = "Source: ${sourcedLayerTMS.source.name}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDownloadClick(sourcedLayerTMS) }
            ) {
                Text("Download")
            }
        }
    }
}