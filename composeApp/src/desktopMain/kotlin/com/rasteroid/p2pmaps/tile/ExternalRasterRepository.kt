package com.rasteroid.p2pmaps.tile

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.source.type.RasterSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private val log = Logger.withTag("external raster repository")

class ExternalRasterRepository {
    companion object {
        // Singleton for now, maybe DI later.
        val instance = ExternalRasterRepository()
    }

    private val _sources = MutableStateFlow(listOf<RasterSource>())
    val sources: StateFlow<List<RasterSource>> = _sources

    fun addSource(source: RasterSource) {
        _sources.value += source
    }

    fun removeSource(source: RasterSource) {
        _sources.value -= source
    }

    fun refresh(coroutineScope: CoroutineScope) {
        log.d { "Refreshing external raster sources" }
        // Run refresh function for each source.
        coroutineScope.launch {
            _sources.value.forEach { source ->
                source.refresh()
            }
        }
    }
}