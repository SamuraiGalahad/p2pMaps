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
import com.rasteroid.p2pmaps.vm.InternalRastersViewModel

@Composable
fun InternalRastersScreen(
    viewModel: InternalRastersViewModel
) {
    if (viewModel.rastersProgress.isEmpty()) {
        NoInternalRastersFound()
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(viewModel.rastersProgress) { raster ->
            BaseRasterCard(raster.info.meta.toReply()) {
                Text(raster.progress)
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