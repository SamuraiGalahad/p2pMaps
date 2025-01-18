package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ViewRastersScreen() {
    NoRastersFoundScreen()
}

@Composable
private fun NoRastersFoundScreen() =
Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
) {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.h6,
    ) {
        Text("No rasters found")
        Row {
            Text("Add a raster under ")
            Icon(
                Icons.Filled.AddLocation,
                "Add a raster"
            )
            Text(" tab")
        }
    }
}