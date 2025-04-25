package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    // Create a grid of fixed size cards, going from left to right, top to bottom.
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

@Composable
fun ProgressRasterCard(
    progressRaster: ProgressLayerTMS
) {
    val layerTMS = progressRaster.layerTMS
    val progressPercentage = floor(progressRaster.current / progressRaster.total.toFloat()) * 100

    RasterCard(
        layer = layerTMS.layer,
        tms = layerTMS.tileMatrixSet
    ) {
        Text(
            text = "${progressRaster.current}/${progressRaster.total} ($progressPercentage%)",
            modifier = Modifier.padding(8.dp)
        )
    }
}