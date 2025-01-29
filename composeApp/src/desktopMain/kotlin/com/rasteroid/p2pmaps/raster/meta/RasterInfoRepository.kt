package com.rasteroid.p2pmaps.raster.meta

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.raster.RasterMeta
import com.rasteroid.p2pmaps.settings.Settings
import com.rasteroid.p2pmaps.settings.ensureDirectoryExists
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.FileInputStream
import java.nio.file.Path

private const val RASTER_INFO_EXTENSION = ".rinfo"
private val log = Logger.withTag("raster repo")

@OptIn(ExperimentalSerializationApi::class)
class RasterInfoRepository {
    companion object {
        val instance = RasterInfoRepository()
    }

    private val rasterInfosPath = Settings.APP_CONFIG_PATH.resolve("rinfo")
    // TODO: may be too large since there could be may files.
    // In the future we probably need to read the directory every time.
    private val rasterInfos: MutableList<RasterInfo> = mutableListOf()

    init {
        ensureDirectoryExists(rasterInfosPath)

        log.d("Loading raster infos from $rasterInfosPath")
        rasterInfosPath.toFile()
            .listFiles { _, name -> name.endsWith(RASTER_INFO_EXTENSION) }
            ?.forEach { file ->
                runCatching {
                    Json.decodeFromStream<RasterInfo>(FileInputStream(file))
                        .let { rasterInfo -> rasterInfos.add(rasterInfo) }
                }.onFailure {
                    log.e("Failed to load raster info from $file", it)
                }
                log.d("Loaded raster info from $file")
            }

        log.i("Added ${rasterInfos.size} raster infos from $rasterInfosPath")
    }

    fun isRasterAvailable(meta: RasterMeta): Long {
        val rasterInfo = rasterInfos.find { it.meta == meta }
        return rasterInfo?.fileSize ?: 0
    }

    fun getRasterPath(meta: RasterMeta): Result<Path> {
        val rasterInfo = rasterInfos.find { it.meta == meta }
        return if (rasterInfo != null) {
            Result.success(rasterInfo.path)
        } else {
            Result.failure(Exception("Raster not found"))
        }
    }
}