package com.rasteroid.p2pmaps.raster.meta

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.config.ensureDirectoryExists
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.FileInputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.outputStream

private const val RASTER_INFO_EXTENSION = ".rinfo"
private val log = Logger.withTag("raster repo")

@OptIn(ExperimentalSerializationApi::class)
class InternalRasterRepository {
    companion object {
        val instance = InternalRasterRepository()
    }

    private val rasterInfosPath = Settings.APP_DATA_PATH.resolve("rinfo")
    // TODO: may be too large since there could be many files.
    // In the future we probably need to read the directory every time.
    val rasterInfos: MutableList<RasterInfo> = mutableListOf()

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

    fun getRasterPath(meta: RasterMeta): Result<Path> {
        val rasterInfo = rasterInfos.find { it.meta == meta }
        return if (rasterInfo != null) {
            Result.success(Paths.get(rasterInfo.path))
        } else {
            Result.failure(Exception("Raster not found"))
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
        }.onSuccess { rasterInfos.add(rasterInfo) }
    }
}