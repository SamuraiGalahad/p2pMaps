package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.tile.LayerTMS
import com.rasteroid.p2pmaps.vm.DownloadRastersViewModel

data class ProgressLayerTMS(
    val layerTMS: LayerTMS,
    val current: Int,
    val total: Int
)

@Composable
fun InternalRastersScreen(
    viewModel: DownloadRastersViewModel
) {
    if (viewModel.rasters.collectAsState().value.isEmpty()) {
        NoInternalRastersFound()
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            viewModel.rasters.collectAsState().value.forEach { layer ->
                Text(
                    text = "${layer.layerTMS.layer} ${layer.layerTMS.tileMatrixSet} ${layer.current}/${layer.total}",
                    color = if (layer.current == layer.total) Color.Green else Color.Red
                )
            }
        }
    }
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