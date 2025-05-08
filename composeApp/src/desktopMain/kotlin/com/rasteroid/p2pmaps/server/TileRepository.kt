package com.rasteroid.p2pmaps.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.config.Settings
import com.rasteroid.p2pmaps.config.ensureDirectoryExists
import com.rasteroid.p2pmaps.tile.*
import java.nio.file.Path
import kotlin.concurrent.fixedRateTimer

private val log = Logger.withTag("tile repo")

data class ProgressLayerTMS(
    val layerTMS: LayerTMS,
    val current: Int,
    val total: Int
)

class TileRepository(
    dataDirectoryPath: Path
) {
    private val layersDirectoryPath = dataDirectoryPath.resolve("layers")
    private val tmsDirectoryPath = dataDirectoryPath.resolve("tms")
    var layers by mutableStateOf(getLayerTMSs())
        private set

    init {
        ensureDirectoryExists(layersDirectoryPath)
        ensureDirectoryExists(tmsDirectoryPath)

        // Load all layer + tms combinations.
        log.i("Initialized ${layers.size} rasters")

        fixedRateTimer(
            name = "Layers refresh job",
            period = 5_000,
            initialDelay = 1_000
        ) {
            layers = getLayerTMSs()
        }
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
        ensureDirectoryExists(tilePath.parent)
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
            raw += "    <TileMatrixSetLink>\n"
            raw += "    <TileMatrixSet>$tms</TileMatrixSet>\n"
            raw += "    </TileMatrixSetLink>\n"
        }

        raw += "</Layer>\n"

        return raw
    }

    // Get all raw layers with TMS links.
    fun getAllLayersTMSLink(): List<String> {
        val layers = layersDirectoryPath.toFile().listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()

        return layers.mapNotNull { layer ->
            getLayerTMSLink(layer)
        }
    }

    // Get the actual number of downloaded tiles (current, total) for a layer.
    fun getLayerTileCount(
        layer: String,
        tms: String,
        format: String
    ): Int {
        // Essentially, count the number of files in the layer directory.
        // Recursively delve into each subdirectory and count the files.
        val layerPath = layersDirectoryPath.resolve(layer).resolve(tms)
        val tileMatrixes = layerPath.toFile().listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()

        var totalTiles = 0
        for (tileMatrix in tileMatrixes) {
            val tileMatrixPath = layerPath.resolve(tileMatrix)
            val tileCols = tileMatrixPath.toFile().listFiles()
                ?.filter { it.isDirectory }
                ?.map { it.name }
                ?: emptyList()

            for (tileCol in tileCols) {
                val tileColPath = tileMatrixPath.resolve(tileCol)
                val tiles = tileColPath.toFile().listFiles()
                    ?.filter { it.extension == format }
                    ?.size
                    ?: 0
                totalTiles += tiles
            }
        }

        return totalTiles
    }

    // Save layer meta
    fun saveLayerMeta(layer: String, layerMeta: LayerMeta) {
        val layerPath = layersDirectoryPath.resolve(layer)
        val infoPath = layerPath.resolve("info.xml")
        if (infoPath.toFile().exists()) {
            // We don't need to edit any meta if it already exists.
            return
        }
        ensureDirectoryExists(layerPath)
        infoPath.toFile().writeText(layerMeta.toXML())
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

    // Get All Tile Matrix Set metas.
    fun getAllTMSMetaRaw(): List<String> {
        val tmsFiles = tmsDirectoryPath.toFile().listFiles()
            ?.filter { it.isFile && it.extension == "xml" }
            ?: emptyList()

        return tmsFiles.map { it.readText() }
    }

    // Save fixed tile matrix set meta.
    fun saveTMSMeta(tms: String, tmsMeta: TMSMeta) {
        val tmsPath = tmsDirectoryPath.resolve("$tms.xml")
        if (tmsPath.toFile().exists()) {
            // We don't need to edit any meta if it already exists.
            return
        }
        ensureDirectoryExists(tmsPath.parent)
        tmsPath.toFile().writeText(tmsMeta.toXML())
    }

    fun getContents(): String {
        // Basically manually building the Contents tag of the WMTS capabilities.
        val contents = StringBuilder("<Contents>\n")
        // Populate with layers.
        val layerTMSLinks = getAllLayersTMSLink()
        for (layerTMS in layerTMSLinks) {
            contents.append(layerTMS)
        }
        // Populate with tile matrix sets.
        val TMSs = getAllTMSMetaRaw()
        for (tms in TMSs) {
            contents.append(tms)
        }
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