package com.rasteroid.p2pmaps.server

import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.config.ensureDirectoryExists
import com.rasteroid.p2pmaps.server.dto.TrackerAnnounce
import com.rasteroid.p2pmaps.tile.*
import java.nio.file.Path

class TileRepository(
    dataDirectoryPath: Path
) {
    private val layersDirectoryPath = dataDirectoryPath.resolve("layers")
    private val tmsDirectoryPath = dataDirectoryPath.resolve("tms")

    init {
        ensureDirectoryExists(layersDirectoryPath)
        ensureDirectoryExists(tmsDirectoryPath)
    }

    /*
    Tile requests.
    */
    fun getTileSize(
        layer: String,
        tileMatrixSet: String,
        tileMatrix: String,
        tileCol: Int,
        tileRow: Int,
        format: TileFormat
    ): Int? {
        val tilePath = resolveTilePath(layer, tileMatrixSet, tileMatrix, tileCol, tileRow, format)
        return if (tilePath.toFile().exists()) {
            tilePath.toFile().length().toInt()
        } else {
            null
        }
    }

    fun getTile(
        layer: String,
        tileMatrixSet: String,
        tileMatrix: String,
        tileCol: Int,
        tileRow: Int,
        format: TileFormat,
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

    fun saveTile(meta: TileMeta, tile: ByteArray) {
        val tilePath = resolveTilePath(
            meta.layer,
            meta.tileMatrixSet,
            meta.tileMatrix,
            meta.tileCol,
            meta.tileRow,
            meta.format
        )
        ensureDirectoryExists(tilePath)
        tilePath.toFile().writeBytes(tile)
    }

    /*
    Layer requests.
    */
    // Construct available unique pairs of layer + TMS.
    fun getLayerTMSs(): List<LayerTMS> {
        // Layers - names of directories in layers directory.
        // TMS - name of a subdirectory inside each layer directory.
        // We need to construct a list of all available pairs of layer + TMS.
        val layers = layersDirectoryPath.toFile().listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()

        val tms = layers.map { layer ->
            val tmsDir = layersDirectoryPath.resolve(layer).toFile().listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?: emptyList()
            tmsDir.map { tms -> LayerTMS(layer, tms) }
        }.flatten()

        return tms
    }

    // Get fixed layer info in string format.
    fun getRawLayerInfo(layer: String): String? {
        // Essentially, read info.xml inside the layer directory.
        val layerPath = layersDirectoryPath.resolve(layer)
        val infoPath = layerPath.resolve("info.xml")
        return if (infoPath.toFile().exists()) {
            infoPath.toFile().readText()
        } else {
            null
        }
    }

    // Get fixed layer info in LayerMeta format.
    fun getLayerInfo(layer: String): LayerMeta? {
        val raw = getRawLayerInfo(layer)
        return if (raw != null) {
            LayerMeta.fromXML(raw)
        } else {
            null
        }
    }

    // Helper function to construct layer meta with TMS links.
    fun getLayerTMSLink(layer: String): String? {
        // Essentially get fixed info and append TMS links to it.
        // TMS links are subdirectories names inside the layer directory.
        var raw = getRawLayerInfo(layer) ?: return null
        val tmsDir = layersDirectoryPath.resolve(layer).toFile().listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()

        // Append TMS links to the raw info.
        tmsDir.forEach { tms ->
            raw += "<TileMatrixSetLink>\n"
            raw += "<TileMatrixSet>$tms</TileMatrixSet>\n"
            raw += "</TileMatrixSetLink>\n"
        }

        return raw
    }

    // Get the download progress (current, total) for a layer.
    fun getLayerProgress(layer: String, tms: String): Pair<Int, Int>? {
        // This is a bit unoptimized, but fine for now.
        // Essentially, compute total tiles from TMS and
        // find current tiles from the layer directory.
        val tmsMeta = getTMSMeta(tms) ?: return null
        val totalTiles = tmsMeta.tileMatrixes.sumOf { tileMatrix ->
            tileMatrix.matrixWidth * tileMatrix.matrixHeight
        }

        // Simply count current tiles by looking at how many
        // files with extension as in TileFormat are in the layer directory.
        val layerPath = layersDirectoryPath.resolve(layer).resolve(tms)
        val currentTiles = layerPath.toFile().listFiles()
            // TODO: For now hardcoding png.
            ?.filter { it.isFile && it.extension == TileFormat.PNG.getExtension() }
            ?.size
            ?: 0

        return Pair(currentTiles, totalTiles)
    }

    /*
    Tile Matrix Set requests.
    */
    // Get fixed Tile Matrix Set meta.
    fun getTMSMeta(tms: String): TMSMeta? {
        // Essentially, read <tms>.xml inside the tms directory.
        val tmsPath = tmsDirectoryPath.resolve("$tms.xml")
        return if (tmsPath.toFile().exists()) {
            val raw = tmsPath.toFile().readText()
            TMSMeta.fromXML(raw)
        } else {
            null
        }
    }

    // Save fixed tile matrix set meta.
    fun saveTMSMeta(tms: String, tmsMeta: TMSMeta) {
        val tmsPath = tmsDirectoryPath.resolve("$tms.xml")
        if (tmsPath.toFile().exists()) {
            // We don't need to edit any meta if it already exists.
            return
        }
        ensureDirectoryExists(tmsPath)
        tmsPath.toFile().writeText(tmsMeta.toXML())
    }

    fun getAnnounce(): TrackerAnnounce {
        // TODO.
        return TrackerAnnounce(
            uuid = "test",
            layers = listOf()
        )
    }

    fun getContents(): String {
        // Basically manually building the Contents tag of the WMTS capabilities.
        val contents = StringBuilder("<Contents>\n")

        contents.append("</Contents>\n")
        return contents.toString()
    }

    private fun resolveTilePath(
        layer: String,
        tileMatrixSet: String,
        tileMatrix: String,
        tileCol: Int,
        tileRow: Int,
        format: TileFormat
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