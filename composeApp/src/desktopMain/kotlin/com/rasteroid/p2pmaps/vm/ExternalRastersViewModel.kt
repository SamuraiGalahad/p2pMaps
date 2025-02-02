package com.rasteroid.p2pmaps.vm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.raster.source.DownloadableRasterMeta
import com.rasteroid.p2pmaps.raster.ExternalRasterRepository
import com.rasteroid.p2pmaps.raster.meta.InternalRasterRepository
import com.rasteroid.p2pmaps.raster.meta.RasterInfo
import io.github.vinceglb.filekit.compose.rememberFileSaverLauncher
import kotlinx.coroutines.launch
import java.io.File

class ExternalRastersViewModel : ViewModel() {
    var rasterList by mutableStateOf<List<DownloadableRasterMeta>>(emptyList())
        private set

    var pickedRaster: DownloadableRasterMeta? = null
    var initialDirectory = Settings.PLATFORM_HOME_PATH

    init {
        ExternalRasterRepository.instance.sources.forEach { source ->
            viewModelScope.launch {
                source.fetch { meta ->
                    rasterList += DownloadableRasterMeta(source, meta)
                }
            }
        }
    }

    fun download(
        resultFile: File,
        raster: DownloadableRasterMeta
    ) {
        viewModelScope.launch {
            val resultStream = resultFile.outputStream()
            resultStream.use {
                raster.source.download(resultStream, raster.meta)
            }
            val rasterSize = resultFile.length()
            InternalRasterRepository.instance.onNewRasterSaved(RasterInfo(
                resultFile.toPath(),
                rasterSize,
                raster.meta
            ))
        }
    }
}