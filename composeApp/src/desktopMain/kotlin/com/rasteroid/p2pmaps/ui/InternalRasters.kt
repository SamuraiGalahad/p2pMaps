package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
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
    NoInternalRastersFound()
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