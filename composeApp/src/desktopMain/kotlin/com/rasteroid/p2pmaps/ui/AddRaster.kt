package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun AddRasterScreen() = Column(
    modifier = Modifier.fillMaxSize()
) {
    // The screen where raster parameters are specified:
    // - bbox: four float coordinates representing the top-left and bottom-right corners of the raster
    // - format: raster file format, string (either "image/png" or "image/jpeg" or "image/tiff").
    // - width: raster width in pixels, float.
    // - height: raster height in pixels. float.
    // - layers: raster layers by WMS provider, a list of strings.
    Text("Add a raster",
        style = MaterialTheme.typography.h6,
    )
    Spacer(modifier = Modifier.height(16.dp))
    RasterParameterScreen(
        onSubmit = { bbox, format, width, height, selectedLayers ->
            println("Submitting raster parameters: " +
                    "bbox=$bbox, format=$format, width=$width, height=$height, layers=$selectedLayers")
        }
    )
}


@Composable
fun RasterParameterScreen(
    onSubmit: (
        bbox: List<Float>,
        format: String,
        width: Float,
        height: Float,
        selectedLayers: List<String>
    ) -> Unit,
    availableFormats: List<String> = listOf("image/png", "image/jpeg", "image/tiff"),
    availableLayers: List<String> = listOf("Layer A", "Layer B", "Layer C", "Layer D")
) {
    var bboxLeft by remember { mutableStateOf("0.0") }
    var bboxTop by remember { mutableStateOf("0.0") }
    var bboxRight by remember { mutableStateOf("0.0") }
    var bboxBottom by remember { mutableStateOf("0.0") }

    var selectedFormat by remember { mutableStateOf(availableFormats.first()) }
    var widthInput by remember { mutableStateOf("512") }
    var heightInput by remember { mutableStateOf("512") }

    // Layers - track which are checked
    val (selectedLayers, setSelectedLayers) = remember {
        mutableStateOf<List<String>>(emptyList())
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(elevation = 2.dp) {
                Column(Modifier.padding(8.dp)) {
                    Text("Bounding Box (Top-Left / Bottom-Right):", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bboxLeft,
                            onValueChange = { bboxLeft = it },
                            label = { Text("Left (X1)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = bboxTop,
                            onValueChange = { bboxTop = it },
                            label = { Text("Top (Y1)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bboxRight,
                            onValueChange = { bboxRight = it },
                            label = { Text("Right (X2)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = bboxBottom,
                            onValueChange = { bboxBottom = it },
                            label = { Text("Bottom (Y2)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }
        item {
            Card(elevation = 2.dp) {
                Column(Modifier.padding(4.dp)) {
                    Text("Output Format:", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedTextField(
                            value = selectedFormat,
                            onValueChange = { /* read-only from dropdown */ },
                            label = { Text("Select Format") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = true }) {
                                    Icon(Icons.Default.Image, contentDescription = null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableFormats.forEach { format ->
                                DropdownMenuItem(onClick = {
                                    selectedFormat = format
                                    expanded = false
                                }) {
                                    Text(text = format)
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Card(elevation = 2.dp) {
                Column(Modifier.padding(4.dp)) {
                    Text("Dimensions:", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = { widthInput = it },
                            label = { Text("Width (px)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = { heightInput = it },
                            label = { Text("Height (px)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }
        }
        item {
            Card(elevation = 2.dp) {
                Column(Modifier.padding(4.dp)) {
                    Text("Choose Raster Layers:", style = MaterialTheme.typography.subtitle1)
                    Spacer(modifier = Modifier.height(4.dp))

                    availableLayers.forEach { layer ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = layer in selectedLayers,
                                onCheckedChange = { isChecked ->
                                    val updatedList = if (isChecked) {
                                        selectedLayers + layer
                                    } else {
                                        selectedLayers - layer
                                    }
                                    setSelectedLayers(updatedList)
                                }
                            )
                            Text(layer)
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = {
                    // Convert text fields to floats safely
                    val bboxValues = listOf(
                        bboxLeft.toFloatOrNull() ?: 0f,
                        bboxTop.toFloatOrNull() ?: 0f,
                        bboxRight.toFloatOrNull() ?: 0f,
                        bboxBottom.toFloatOrNull() ?: 0f
                    )
                    val w = widthInput.toFloatOrNull() ?: 0f
                    val h = heightInput.toFloatOrNull() ?: 0f

                    onSubmit(
                        bboxValues,
                        selectedFormat,
                        w,
                        h,
                        selectedLayers
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Generate Raster")
            }
        }
    }
}