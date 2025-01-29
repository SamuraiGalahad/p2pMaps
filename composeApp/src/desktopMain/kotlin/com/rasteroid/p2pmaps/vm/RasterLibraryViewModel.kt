package com.rasteroid.p2pmaps.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rasteroid.p2pmaps.raster.SourcedRasterMeta
import com.rasteroid.p2pmaps.raster.RasterRepository
import kotlinx.coroutines.launch

class RasterLibraryViewModel(
    private val repository: RasterRepository
): ViewModel() {
    var rasterList by mutableStateOf<List<SourcedRasterMeta>>(emptyList())
        private set

    init {
        viewModelScope.launch {
            rasterList = repository.getAllRasters()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            rasterList = repository.getAllRasters()
        }
    }

    fun download(raster: SourcedRasterMeta) {
        viewModelScope.launch {
            repository.downloadRaster(raster)
        }
    }
}