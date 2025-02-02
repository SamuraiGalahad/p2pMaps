package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.raster.source.DownloadableRasterMeta
import com.rasteroid.p2pmaps.vm.ExternalRastersViewModel
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher


@Composable
fun ExternalRastersScreen(
    viewModel: ExternalRastersViewModel
) {
    val fileSaverLauncher = rememberFileSaverLauncher {
        if (it != null) {
            viewModel.initialDirectory = it.file.parent
            viewModel.download(it.file, viewModel.pickedRaster!!)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(viewModel.rasterList) { raster ->
            RasterCard(raster) {
                viewModel.pickedRaster = raster
                fileSaverLauncher.launch(
                    baseName = "raster",
                    extension = ".${raster.meta.format}",
                    initialDirectory = viewModel.initialDirectory
                )
            }
        }
    }
}


@Composable
fun RasterCard(
    raster: DownloadableRasterMeta,
    onDownloadClicked: () -> Unit
) {
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
                Text("Name: ${raster.source.name}")
                Text("Type: ${raster.source.type}")
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
