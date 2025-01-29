package com.rasteroid.p2pmaps.raster

class RasterRepository {
    companion object {
        // Singleton for now, maybe DI later.
        val instance = RasterRepository()
            .addSource(SavedPeersRasterSource())
    }

    private var currentSourceId = 0
    private val sources: MutableMap<Int, RasterSource> = mutableMapOf()

    fun addSource(source: RasterSource): RasterRepository {
        sources[currentSourceId++] = source
        return this
    }

    fun removeSource(rasterSourceId: Int): RasterRepository {
        sources.remove(rasterSourceId)
        return this
    }

    suspend fun getAllRasters(): List<SourcedRasterMeta> =
        sources.flatMap { (sourceId, source) ->
            source.fetchRasters().map { meta ->
                SourcedRasterMeta(sourceId, source.name, source.type, meta)
            }
        }

    suspend fun downloadRaster(raster: SourcedRasterMeta) {
        val matchingSource = sources[raster.sourceId]
            ?: error("No source found for raster: $raster")
        matchingSource.downloadRaster(raster.meta)
    }
}