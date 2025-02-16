package com.rasteroid.p2pmaps.vm

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rasteroid.p2pmaps.raster.meta.InternalRasterRepository
import com.rasteroid.p2pmaps.raster.meta.RasterInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class InternalRastersViewModel : ViewModel() {
    data class RasterInfoProgress(
        val info: RasterInfo,
        val progress: String
    )

    private val rasterInfos = InternalRasterRepository.instance.rasterInfos
    private val _rastersProgress = mutableStateListOf<RasterInfoProgress>()
    val rastersProgress: SnapshotStateList<RasterInfoProgress> = _rastersProgress

    init {
        viewModelScope.launch {
            while (isActive) {
                val updated = rasterInfos.map { info ->
                    val progressString = getRasterProgress(info)
                    RasterInfoProgress(
                        info = info,
                        progress = progressString
                    )
                }
                _rastersProgress.clear()
                _rastersProgress.addAll(updated)
                delay(1000)
            }
        }
    }

    fun getRasterProgress(info: RasterInfo): String {
        // Result in the format of: "fileLength / info.fileSize (% progress)"
        return InternalRasterRepository.instance.getRasterSize(info.path)
            .map { fileLength ->
                val progress = (fileLength * 100 / info.fileSize).toInt()
                "$fileLength / ${info.fileSize} ($progress%)"
            }
            .getOrDefault("0 / ${info.fileSize} (0%)")
    }
}