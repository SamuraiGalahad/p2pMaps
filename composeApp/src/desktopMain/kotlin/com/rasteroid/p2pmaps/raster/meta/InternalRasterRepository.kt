package com.rasteroid.p2pmaps.raster.meta

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.config.ensureDirectoryExists
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.FileInputStream
import java.nio.file.Paths
import kotlin.io.path.outputStream

private const val RASTER_INFO_EXTENSION = ".rinfo"
private val log = Logger.withTag("internal raster repo")

@OptIn(ExperimentalSerializationApi::class)
class InternalRasterRepository {
    companion object {
        val instance = InternalRasterRepository()
    }

    private val rasterInfosPath = Settings.APP_DATA_PATH.resolve("rinfo")
    private val _rasterInfos = mutableStateListOf<RasterInfo>()
    val rasterInfos: SnapshotStateList<RasterInfo> = _rasterInfos

    init {
        ensureDirectoryExists(rasterInfosPath)

        log.d("Loading raster infos from $rasterInfosPath")
        rasterInfosPath.toFile()
            .listFiles { _, name -> name.endsWith(RASTER_INFO_EXTENSION) }
            ?.forEach { file ->
                runCatching {
                    Json.decodeFromStream<RasterInfo>(FileInputStream(file))
                        .let { rasterInfo -> _rasterInfos.add(rasterInfo) }
                }.onFailure {
                    log.e("Failed to load raster info from $file", it)
                }
                log.d("Loaded raster info from $file")
            }

        log.i("Added ${_rasterInfos.size} raster infos from $rasterInfosPath")
    }

    fun getRasterPath(meta: RasterMeta): Result<String> {
        val rasterInfo = _rasterInfos.find { it.meta == meta }
        return if (rasterInfo != null) {
            Result.success(rasterInfo.path)
        } else {
            Result.failure(Exception("Raster not found"))
        }
    }

    fun getRasterSize(path: String): Result<Long> {
        return runCatching {
            Paths.get(path).toFile().length()
        }
    }

    fun onNewRasterSaved(rasterInfo: RasterInfo) {
        val id = rasterInfo.hashCode()
        val filename = "$id$RASTER_INFO_EXTENSION"

        runCatching {
            val stream = rasterInfosPath.resolve(filename).outputStream()
            stream.use {
                Json.encodeToStream(rasterInfo, stream)
            }
        }.onSuccess { _rasterInfos.add(rasterInfo) }
    }
}