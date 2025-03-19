package com.rasteroid.p2pmaps.server

import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.config.ensureDirectoryExists
import com.rasteroid.p2pmaps.server.dto.TrackerAnnounce
import com.rasteroid.p2pmaps.server.dto.TrackerLayer
import com.rasteroid.p2pmaps.server.dto.TrackerTile
import com.rasteroid.p2pmaps.tile.RasterFormat
import com.rasteroid.p2pmaps.tile.RasterReply
import com.rasteroid.p2pmaps.tile.TileMatrixSet
import com.rasteroid.p2pmaps.tile.TileMeta
import nl.adaptivity.xmlutil.serialization.XML
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

    fun getAnnounce(): TrackerAnnounce {
        val layers = mutableListOf<TrackerLayer>()
        layersDirectoryPath.toFile().listFiles()?.filter { it.isDirectory }?.forEach { layerDir ->
            val layerName = layerDir.name
            val layerMatrix = mutableMapOf<String, MutableList<TrackerTile>>()
            // Under each layer dir, we expect subdirectories for tile_matrix_set_name
            layerDir.listFiles()?.filter { it.isDirectory }?.forEach { tileMatrixSetDir ->
                val tileMatrixSetName = tileMatrixSetDir.name
                // Then each subdirectory is tile_matrix
                tileMatrixSetDir.listFiles()?.filter { it.isDirectory }?.forEach { tileMatrixDir ->
                    val tileMatrixName = tileMatrixDir.name  // e.g. "0"

                    // Then inside that, each subdirectory is tile_col
                    tileMatrixDir.listFiles()?.filter { it.isDirectory }?.forEach { tileColDir ->
                        val col = tileColDir.name.toInt()

                        // Inside tile_col, each file is tile_row.format
                        tileColDir.listFiles()?.filter { it.isFile }?.forEach { tileFile ->
                            val fileName = tileFile.name  // e.g. "123.png"
                            val dotIndex = fileName.lastIndexOf('.')
                            if (dotIndex != -1) {
                                val rowString = fileName.substring(0, dotIndex)
                                val row = rowString.toInt()
                                val fileFormat = fileName.substring(dotIndex + 1)

                                val tile = TrackerTile(
                                    col,
                                    row,
                                    fileFormat
                                )

                                // Ensure we have a list in the map for this tileMatrix
                                layerMatrix.computeIfAbsent(tileMatrixName) {
                                    mutableListOf()
                                }.add(tile)
                            }
                        }
                    }
                }

                // Add this layer’s data to our layers list
                layers.add(TrackerLayer(
                    layerName,
                    tileMatrixSetName,
                    layerMatrix
                ))
            }
        }

        return TrackerAnnounce(
            uuid = Settings.PEER_ID,
            layers = layers
        )
    }

    fun getLayerInfo(layer: String): RasterReply? {
        val layerInfoPath = layersDirectoryPath.resolve(layer).resolve("info.xml")
        return if (layerInfoPath.toFile().exists()) {
            val layerInfo = layerInfoPath.toFile().readText()
            XML.decodeFromString(layerInfo)
        } else {
            null
        }
    }

    fun saveLayerInfo(layer: String, info: RasterReply) {
        val layerPath = layersDirectoryPath.resolve(layer)
        ensureDirectoryExists(layerPath)
        val layerInfoPath = layerPath.resolve("info.xml")
        layerInfoPath.toFile().writeText(XML.encodeToString(info))
    }

    fun saveTileMatrixSetInfo(
        layer: String,
        tileMatrixSetInfo: String,
        info: TileMatrixSet
    ) {
        val tileMatrixSetPath = layersDirectoryPath.resolve(layer).resolve(tileMatrixSetInfo)
        ensureDirectoryExists(tileMatrixSetPath)
        val tileMatrixSetInfoPath = tileMatrixSetPath.resolve("info.xml")
        tileMatrixSetInfoPath.toFile().writeText(XML.encodeToString(info))
    }

    fun saveTile(meta: TileMeta, tile: ByteArray) {
        val tilePath = resolveTilePath(
            meta.rasterMeta.layer,
            meta.rasterMeta.tileMatrixSet,
            meta.tileMatrix,
            meta.tileCol,
            meta.tileRow,
            meta.format
        )
        ensureDirectoryExists(tilePath)
        tilePath.toFile().writeBytes(tile)
    }

    fun getRasters(): List<RasterReply> {
        val layerPaths = layersDirectoryPath.listDirectoryEntries()
        val rasters = mutableListOf<RasterReply>()
        for (layer in layerPaths) {
            val layerInfoPath = layer.resolve("info.xml")
            if (layerInfoPath.toFile().exists()) {
                val layerInfo = layerInfoPath.toFile().readText()
                // Serialize XML text into RasterReply.
                rasters.add(XML.decodeFromString(layerInfo))
            }
        }
        return rasters
    }

    fun getTileMatrixSetInfo(
        layer: String,
        tileMatrixSet: String
    ): TileMatrixSet? {
        val tileMatrixSetPath = layersDirectoryPath
            .resolve(layer)
            .resolve(tileMatrixSet)
            .resolve("info.xml")
        return if (tileMatrixSetPath.toFile().exists()) {
            val tileMatrixSetInfo = tileMatrixSetPath.toFile().readText()
            XML.decodeFromString(tileMatrixSetInfo)
        } else {
            null
        }
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
        format: RasterFormat,
        offsetBytes: Int = 0,
        limitBytes: Int = Int.MAX_VALUE
    ): ByteArray? {
        val tilePath = resolveTilePath(layer, tileMatrixSet, tileMatrix, tileCol, tileRow, format)
        return if (tilePath.toFile().exists()) {
            tilePath
                .toFile()
                .readBytes()
                .sliceArray(offsetBytes until minOf(offsetBytes + limitBytes,
                    tilePath.toFile().length().toInt()))
        } else {
            null
        }
    }

    fun getTileSize(
        layer: String,
        tileMatrixSet: String,
        tileMatrix: String,
        tileCol: Int,
        tileRow: Int,
        format: RasterFormat
    ): Int? {
        val tilePath = resolveTilePath(layer, tileMatrixSet, tileMatrix, tileCol, tileRow, format)
        return if (tilePath.toFile().exists()) {
            tilePath.toFile().length().toInt()
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