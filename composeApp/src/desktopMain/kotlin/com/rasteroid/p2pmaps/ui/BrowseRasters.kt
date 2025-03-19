package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.tile.RasterMeta
import com.rasteroid.p2pmaps.tile.source.type.RasterSource
import com.rasteroid.p2pmaps.vm.BrowseRastersViewModel

data class SourcedRasterMeta(
    val source: RasterSource,
    val meta: RasterMeta
)

@Composable
fun BrowseRastersScreen(
    viewModel: BrowseRastersViewModel
) {
    RasterGridScreen(viewModel)
}

@Composable
fun RasterGridScreen(viewModel: BrowseRastersViewModel) {
    // Collect the list of sources
    val allSources by viewModel.sources.collectAsState()

    // A single grid that displays each RasterSource in its own card
    RasterGrid(
        sources = allSources,
        onDownloadClick = { sourcedRasterMeta -> viewModel.onDownloadSource(sourcedRasterMeta) }
    )
}

@Composable
fun RasterGrid(
    sources: List<RasterSource>,
    onDownloadClick: (SourcedRasterMeta) -> Unit
) {
    // Combine sources and their metas into a list of SourcedRasterMeta.
    val sourcedRasterMetas = mutableListOf<SourcedRasterMeta>()
    sources.forEach { source ->
        val allMetas = source.rasters.collectAsState()
        allMetas.value.forEach { meta ->
            sourcedRasterMetas.add(SourcedRasterMeta(source, meta))
        }
    }

    // A simple 2-column grid
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(sourcedRasterMetas) { sourcedRasterMeta ->
            RasterCard(sourcedRasterMeta, onDownloadClick)
        }
    }
}

@Composable
fun RasterCard(
    sourcedRasterMeta: SourcedRasterMeta,
    onDownloadClick: (SourcedRasterMeta) -> Unit
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Layer: ${sourcedRasterMeta.meta.layer}")
            Text(text = "TileMatrixSet: ${sourcedRasterMeta.meta.tileMatrixSet}")
            Text(text = "Source: ${sourcedRasterMeta.source.name}")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDownloadClick(sourcedRasterMeta) }
            ) {
                Text("Download")
            }
        }
    }
}