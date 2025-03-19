package com.rasteroid.p2pmaps.vm

import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.tile.ExternalRasterRepository
import com.rasteroid.p2pmaps.tile.source.type.RasterSource
import com.rasteroid.p2pmaps.ui.SourcedRasterMeta
import kotlinx.coroutines.flow.StateFlow

class BrowseRastersViewModel : ViewModel() {
    val sources: StateFlow<List<RasterSource>> = ExternalRasterRepository.instance.sources

    fun onDownloadSource(sourcedRasterMeta: SourcedRasterMeta) {

    }
}