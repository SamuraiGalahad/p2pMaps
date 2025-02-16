package com.rasteroid.p2pmaps.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.raster.ExternalRasterRepository
import com.rasteroid.p2pmaps.raster.meta.InternalRasterRepository
import com.rasteroid.p2pmaps.raster.meta.RasterInfo
import com.rasteroid.p2pmaps.raster.meta.RasterMeta
import com.rasteroid.p2pmaps.raster.source.DownloadableRasterMeta
import com.rasteroid.p2pmaps.raster.source.type.RasterSource
import kotlinx.coroutines.launch
import java.io.File

class ExternalRastersViewModel : ViewModel() {
    val rasters = ExternalRasterRepository.instance.rasters

    var showDialog by mutableStateOf(false)
    var pickedRaster: DownloadableRasterMeta? = null
    var pickedFile: File? = null
    var initialDirectory = Settings.PLATFORM_HOME_PATH

    fun closeDownloadDialog() {
        showDialog = false
    }

    fun download(
        resultFile: File,
        source: RasterSource,
        meta: RasterMeta
    ) {
        showDialog = false

        viewModelScope.launch {
            val resultStream = resultFile.outputStream()
            resultStream.use {
                source.download(resultStream, meta) { rasterSize ->
                    InternalRasterRepository.instance.onNewRasterSaved(
                        RasterInfo(
                            resultFile.absolutePath,
                            rasterSize,
                            meta
                        )
                    )
                }
            }
        }
    }
}