package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
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
        Text(text = "Source: ${sourcedLayerTMS.source.name}")
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onDownloadClick(sourcedLayerTMS) }
        ) {
            Text("Download")
        }
    }
}