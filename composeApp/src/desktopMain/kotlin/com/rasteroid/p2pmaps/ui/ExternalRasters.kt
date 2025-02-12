package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import com.rasteroid.p2pmaps.raster.meta.BoundingBox
import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import com.rasteroid.p2pmaps.raster.source.DownloadableRasterMeta
import com.rasteroid.p2pmaps.vm.ExternalRastersViewModel
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher


@Composable
fun ExternalRastersScreen(
    viewModel: ExternalRastersViewModel
) {
    val rasters by viewModel.rasters.collectAsState()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 300.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(rasters) { raster ->
            RasterCard(raster) {
                viewModel.showDialog = true
                viewModel.pickedRaster = raster
            }
        }
    }

    if (viewModel.showDialog) {
        DialogWindow(
            onCloseRequest = { viewModel.showDialog = false }, // close when user clicks outside or X
            title = "Enter Raster Meta Info",
        ) {
            Surface {
                Column(modifier = Modifier.padding(16.dp)) {
                    val meta = viewModel.pickedRaster!!.meta
                    var fileFormat by remember { mutableStateOf(meta.format ?: "") }
                    var rasterWidth by remember { mutableStateOf(meta.width?.toString() ?: "") }
                    var rasterHeight by remember { mutableStateOf(meta.height?.toString() ?: "") }
                    var time by remember { mutableStateOf(meta.time ?: "") }
                    var layers by remember { mutableStateOf(meta.layers?.joinToString(", ") ?: "") }

                    val bb = meta.boundingBox
                    var boundingBoxMinX by remember { mutableStateOf(bb?.minX?.toString() ?: "") }
                    var boundingBoxMinY by remember { mutableStateOf(bb?.minY?.toString() ?: "") }
                    var boundingBoxMaxX by remember { mutableStateOf(bb?.maxX?.toString() ?: "") }
                    var boundingBoxMaxY by remember { mutableStateOf(bb?.maxY?.toString() ?: "") }

                    var filePath by remember { mutableStateOf("") }
                    val fileSaverLauncher = rememberFileSaverLauncher {
                        if (it != null) {
                            filePath = it.file.absolutePath
                            viewModel.pickedFile = it.file
                            viewModel.initialDirectory = it.file.parent
                        }
                    }

                    TextField(
                        value = fileFormat,
                        onValueChange = { fileFormat = it },
                        label = { Text("File Format") },
                        readOnly = meta.format != null
                    )
                    TextField(
                        value = rasterWidth,
                        onValueChange = { rasterWidth = it },
                        label = { Text("Raster Width") },
                        readOnly = meta.width != null
                    )
                    TextField(
                        value = rasterHeight,
                        onValueChange = { rasterHeight = it },
                        label = { Text("Raster Height") },
                        readOnly = meta.height != null
                    )
                    TextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time") },
                        readOnly = meta.time != null
                    )
                    TextField(
                        value = layers,
                        onValueChange = { layers = it },
                        label = { Text("Layers") },
                        readOnly = meta.layers != null
                    )
                    Column {
                        Row {
                            TextField(
                                value = boundingBoxMinX,
                                onValueChange = { boundingBoxMinX = it },
                                label = { Text("Min X") },
                                readOnly = meta.boundingBox != null
                            )
                            TextField(
                                value = boundingBoxMinY,
                                onValueChange = { boundingBoxMinY = it },
                                label = { Text("Min Y") },
                                readOnly = meta.boundingBox != null
                            )
                        }
                        Row {
                            TextField(
                                value = boundingBoxMaxX,
                                onValueChange = { boundingBoxMaxX = it },
                                label = { Text("Max X") },
                                readOnly = meta.boundingBox != null
                            )
                            TextField(
                                value = boundingBoxMaxY,
                                onValueChange = { boundingBoxMaxY = it },
                                label = { Text("Max Y") },
                                readOnly = meta.boundingBox != null
                            )
                        }
                    }

                    // Text field for file path, can't be typed in by user.
                    // On click it should open a file dialog to choose a file.
                    Button(
                        onClick = {
                            fileSaverLauncher.launch(
                                baseName = "raster",
                                extension = fileFormat,
                                initialDirectory = viewModel.initialDirectory
                            )
                        }
                    ) {
                        Text(filePath.ifEmpty { "Choose File" })
                    }

                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = viewModel::closeDownloadDialog) {
                            Text("Cancel")
                        }
                        TextButton(enabled = filePath.isNotEmpty() && fileFormat.isNotEmpty()
                                && rasterWidth.toIntOrNull() != null && rasterHeight.toIntOrNull() != null
                                && time.isNotEmpty() && viewModel.pickedFile != null
                                && boundingBoxMinX.toDoubleOrNull() != null && boundingBoxMinY.toDoubleOrNull() != null
                                && boundingBoxMaxX.toDoubleOrNull() != null && boundingBoxMaxY.toDoubleOrNull() != null
                                && layers.isNotEmpty(),
                            onClick = { viewModel.download(
                                viewModel.pickedFile!!,
                                viewModel.pickedRaster!!.source,
                                RasterMeta(
                                    format = fileFormat,
                                    width = rasterWidth.toInt(),
                                    height = rasterHeight.toInt(),
                                    time = time,
                                    layers = layers.split(",").map { it.trim() },
                                    boundingBox = BoundingBox(
                                        minX = boundingBoxMinX.toDouble(),
                                        minY = boundingBoxMinY.toDouble(),
                                        maxX = boundingBoxMaxX.toDouble(),
                                        maxY = boundingBoxMaxY.toDouble()
                                    )
                                )
                            )}
                        ) {
                            Text("Download")
                        }
                    }
                }
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
