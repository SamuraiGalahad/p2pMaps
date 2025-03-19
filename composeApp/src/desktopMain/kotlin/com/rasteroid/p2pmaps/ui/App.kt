package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.vm.BrowseRastersViewModel
import com.rasteroid.p2pmaps.vm.DownloadRastersViewModel
import com.rasteroid.p2pmaps.vm.LogsViewModel
import com.rasteroid.p2pmaps.vm.SettingsScreenViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    // We create the screens to so that their state is preserved
    // when switching between them.
    val internalRasterVM = remember { DownloadRastersViewModel() }
    val externalRasterVM = remember { BrowseRastersViewModel() }
    val settingsVM = remember { SettingsScreenViewModel() }
    val logsVM = remember { LogsViewModel() }

    val screens = listOf(
        Triple(AppScreen.INTERNAL_RASTERS, Icons.Filled.Menu, "Library"),
        Triple(AppScreen.EXTERNAL_RASTERS, Icons.Filled.ImageSearch, "Browser"),
        Triple(AppScreen.SETTINGS, Icons.Filled.Settings, "Settings"),
        Triple(AppScreen.LOGS, Icons.Filled.Code, "Logs")
    )

    var currentScreen by remember { mutableStateOf(AppScreen.INTERNAL_RASTERS) }

    MaterialTheme {
        Row {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .width(50.dp)
                    .fillMaxHeight()
                    .background(color = Color(0xff202020))
            ) {
                screens.forEach { (screen, icon, description) ->
                    MainBarScreenSwitch(
                        icon,
                        description,
                        isSelected = currentScreen == screen
                    ) {
                        currentScreen = screen
                    }
                }
            }
            Surface(
                modifier = Modifier.padding(10.dp)
            ) {
                when (currentScreen) {
                    AppScreen.INTERNAL_RASTERS -> InternalRastersScreen(internalRasterVM)
                    AppScreen.EXTERNAL_RASTERS -> BrowseRastersScreen(externalRasterVM)
                    AppScreen.SETTINGS -> SettingsScreen(settingsVM)
                    AppScreen.LOGS -> LogsScreen(logsVM)
                }
            }
        }
    }
}

enum class AppScreen {
    INTERNAL_RASTERS,
    EXTERNAL_RASTERS,
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
