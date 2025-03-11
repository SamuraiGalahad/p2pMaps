package com.rasteroid.p2pmaps.raster

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.raster.source.DownloadableRasterMeta
import com.rasteroid.p2pmaps.raster.source.type.RasterSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val log = Logger.withTag("external raster repository")

class ExternalRasterRepository {
    companion object {
        // Singleton for now, maybe DI later.
        val instance = ExternalRasterRepository()
    }

    private val sources = mutableListOf<RasterSource>()
    private val _rasters = MutableStateFlow<List<DownloadableRasterMeta>>(emptyList())
    val rasters: StateFlow<List<DownloadableRasterMeta>> = _rasters.asStateFlow()

    fun addSource(source: RasterSource) {
        sources.add(source)
    }

    fun removeSource(source: RasterSource) {
        sources.remove(source)
    }

    fun refresh(coroutineScope: CoroutineScope) {
        log.i("Refreshing external rasters")
        _rasters.value = emptyList()
        log.d("fetching rasters from ${sources.size} sources")
        sources.forEach {
            coroutineScope.launch {
                log.d("fetching rasters from ${it.name}")
                it.fetch { meta ->
                    _rasters.value += DownloadableRasterMeta(it, meta)
                }
            }
        }
    }
}