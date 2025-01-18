package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf(AppScreen.VIEW_RASTERS) }

    MaterialTheme {
        Row {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight()
                    .background(color = Color(0xff202020))
            ) {
                MainBarScreenSwitch(
                    Icons.Filled.Menu,
                    "View downloaded rasters",
                    isSelected = currentScreen == AppScreen.VIEW_RASTERS
                ) {
                    currentScreen = AppScreen.VIEW_RASTERS
                }
                MainBarScreenSwitch(
                    Icons.Filled.AddLocation,
                    "Add a raster",
                    isSelected = currentScreen == AppScreen.ADD_RASTER
                ) {
                    currentScreen = AppScreen.ADD_RASTER
                }
                MainBarScreenSwitch(
                    Icons.Filled.Settings,
                    "Settings",
                    isSelected = currentScreen == AppScreen.SETTINGS
                ) {
                    currentScreen = AppScreen.SETTINGS
                }
                MainBarScreenSwitch(
                    Icons.Filled.Code,
                    "Logs",
                    isSelected = currentScreen == AppScreen.LOGS
                ) {
                    currentScreen = AppScreen.LOGS
                }
            }
            Surface(
                modifier = Modifier.padding(10.dp)
            ) {
                when (currentScreen) {
                    AppScreen.VIEW_RASTERS -> ViewRastersScreen()
                    AppScreen.ADD_RASTER -> AddRasterScreen()
                    AppScreen.SETTINGS -> SettingsScreen()
                    AppScreen.LOGS -> LogsScreen()
                }
            }
        }
    }
}

enum class AppScreen {
    VIEW_RASTERS,
    ADD_RASTER,
    SETTINGS,
    LOGS
}

@Composable
fun MainBarScreenSwitch(
    icon: ImageVector,
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    val backgroundColor = if (isSelected) Color.DarkGray else Color.Transparent

    // TODO: Add a tooltip for better accessibility.
    IconButton(
        onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(2.dp)
            .background(backgroundColor, MaterialTheme.shapes.small)
    ) {
        Icon(
            icon,
            contentDescription,
            tint = Color.White
        )
    }
}
