package com.rasteroid.p2pmaps.tile

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.source.type.RasterSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private val log = Logger.withTag("external raster repository")

data class SourcedLayerTMS(
    val source: RasterSource,
    val layerTMS: LayerTMS
)

class ExternalRasterRepository {
    companion object {
        // Singleton for now, maybe DI later.
        val instance = ExternalRasterRepository()
    }

    private val sourceRefreshDelayMillis = 30 * 1000L // 30 seconds
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _sourceJobs = mutableMapOf<RasterSource, Job>()
    private val _rasters = MutableStateFlow<Set<SourcedLayerTMS>>(emptySet())
    // rasters are sorted by layer and TMS for stable ordering.
    val rasters = _rasters.map {
        it.sortedBy { raster -> raster.source.name }
            .sortedBy { raster -> raster.layerTMS.layer }
            .sortedBy { raster -> raster.layerTMS.tileMatrixSet }
    }.stateIn(mainScope, SharingStarted.Eagerly, emptyList())

    fun addSource(source: RasterSource) {
        log.d { "Adding source: ${source.name}" }
        // Launch a collector for the source.
        val job = launchCollector(source)
        _sourceJobs[source] = job
    }

    fun removeSource(source: RasterSource) {
        log.d { "Removing source: ${source.name}" }
        // Cancel the collector for the source.
        _sourceJobs[source]?.cancel()
        _sourceJobs.remove(source)
    }

    fun cancel() = mainScope.cancel()

    private fun launchCollector(
        source: RasterSource
    ): Job {
        log.d { "Launching collector for source: ${source.name}" }
        // Launch a coroutine to collect the flow.
        val job = mainScope.launch {
            while (mainScope.isActive) {
                source.getRasters()
                    .onSuccess {
                        log.d("Received rasters from source: ${source.name}")
                        mergeRasters(source, it)
                    }
                    .onFailure {
                        log.e("Failed to get rasters from source: ${source.name}")
                    }
                delay(sourceRefreshDelayMillis)
            }
        }
        return job
    }

    private suspend fun mergeRasters(source: RasterSource, rasters: List<LayerTMS>) = withContext(NonCancellable) {
        _rasters.update {
            current -> current.toMutableSet().apply {
                rasters.forEach { layerTMS ->
                    add(SourcedLayerTMS(source, layerTMS))
                }
            }
        }
    }
}