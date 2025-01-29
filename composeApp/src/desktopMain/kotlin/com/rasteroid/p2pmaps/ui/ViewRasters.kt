package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.raster.SourcedRasterMeta
import com.rasteroid.p2pmaps.raster.RasterRepository
import com.rasteroid.p2pmaps.vm.RasterLibraryViewModel


@Composable
fun ViewRastersScreen() {
    val repository = remember { RasterRepository.instance }
    val viewModel = remember { RasterLibraryViewModel(repository) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(viewModel.rasterList) { raster ->
            RasterCard(raster) {
                viewModel.download(raster)
            }
        }
    }
}


@Composable
fun RasterCard(raster: SourcedRasterMeta, onDownloadClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("ID: ${raster.sourceId}", style = MaterialTheme.typography.subtitle1)
                Text("Name: ${raster.sourceName}")
                Text("Type: ${raster.sourceType}")
                // Show meta fields. Adjust or expand as needed.
                Text("Resolution: ${raster.meta.width} x ${raster.meta.height}")
                Text("Bounding Box: ${raster.meta.boundingBox}")
            }

            // Download button aligned to bottom-right
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                Button(onClick = onDownloadClicked) {
                    Text("Download")
                }
            }
        }
    }
}
