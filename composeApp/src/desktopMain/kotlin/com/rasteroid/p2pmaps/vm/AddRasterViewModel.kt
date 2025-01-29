package com.rasteroid.p2pmaps.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rasteroid.p2pmaps.raster.RasterMeta
import com.rasteroid.p2pmaps.raster.RasterRepository
import com.rasteroid.p2pmaps.raster.RasterSourceType
import com.rasteroid.p2pmaps.raster.SourcedRasterMeta
import kotlinx.coroutines.launch

class AddRasterViewModel : ViewModel() {
    fun downloadRaster(meta: RasterMeta) {
        viewModelScope.launch {
            RasterRepository.instance.downloadRaster(
                SourcedRasterMeta(
                    sourceId = 0, // TODO: placeholder
                    sourceName = "Saved Peers", // TODO: placeholder
                    sourceType = RasterSourceType.PEER, // TODO: placeholder
                    meta = meta
                )
            )
        }
    }
}