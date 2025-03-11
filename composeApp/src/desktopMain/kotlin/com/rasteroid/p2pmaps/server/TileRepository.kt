package com.rasteroid.p2pmaps.server

import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.config.ensureDirectoryExists
import com.rasteroid.p2pmaps.raster.RasterFormat
import java.nio.file.Path
import kotlin.io.path.forEachDirectoryEntry
import kotlin.io.path.listDirectoryEntries

class TileRepository(
    dataDirectoryPath: Path
) {
    private val layersDirectoryPath = dataDirectoryPath.resolve("layers")

    init {
        ensureDirectoryExists(layersDirectoryPath)
    }

    fun getContents(): String {
        // Basically manually building the Contents tag of the WMTS capabilities.
        val contents = StringBuilder("<Contents>\n")
        val layerPaths = layersDirectoryPath.listDirectoryEntries()
        for (layer in layerPaths) {
            val layerInfoPath = layer.resolve("info.xml")
            if (layerInfoPath.toFile().exists()) {
                val layerInfo = layerInfoPath.toFile().readText()
                contents.append("<Layer>\n")
                contents.append(layerInfo)
                contents.append("</Layer>\n")
            }
        }

        for (layer in layerPaths) {
            layer.forEachDirectoryEntry { tileMatrixSetPath ->
                val tileMatrixSetInfoPath = layer
                    .resolve(tileMatrixSetPath)
                    .resolve("info.xml")
                if (tileMatrixSetInfoPath.toFile().exists()) {
                    val tileMatrixSetInfo = tileMatrixSetInfoPath.toFile().readText()
                    contents.append("<TileMatrixSet>\n")
                    contents.append(tileMatrixSetInfo)
                    contents.append("</TileMatrixSet>\n")
                }
            }
        }

        contents.append("</Contents>\n")

        return contents.toString()
    }

    fun getTile(
        layer: String,
        tileMatrixSet: String,
        tileMatrix: String,
        tileCol: Int,
        tileRow: Int,
        format: RasterFormat
    ): ByteArray? {
        val tilePath = resolveTilePath(layer, tileMatrixSet, tileMatrix, tileCol, tileRow, format)
        return if (tilePath.toFile().exists()) {
            tilePath.toFile().readBytes()
        } else {
            null
        }
    }

    private fun resolveTilePath(
        layer: String,
        tileMatrixSet: String,
        tileMatrix: String,
        tileCol: Int,
        tileRow: Int,
        format: RasterFormat
    ): Path =
        // We're basically following OpenStreetMap's Z-X-Y layout.
        layersDirectoryPath
            .resolve(layer)
            .resolve(tileMatrixSet)
            .resolve(tileMatrix)
            .resolve(tileCol.toString())
            .resolve("$tileRow.${format.getExtension()}")

    companion object {
        val instance = TileRepository(Settings.APP_DATA_PATH.resolve("data"))
    }
}