package com.rasteroid.p2pmaps.tile

import kotlinx.serialization.Serializable

@Serializable
data class BoundingBox(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
)

@Serializable
// Layer + TMS pair, used for querying available unique layer + TMS pairs from a peer.
data class LayerTMS(
    val layer: String,
    val tileMatrixSet: String
)

@Serializable
// Meta used to identify a specific tile.
data class TileMeta(
    val layer: String,
    val tileMatrixSet: String,
    val tileMatrix: String,
    val tileRow: Int,
    val tileCol: Int,
    val format: TileFormat
)

@Serializable
// Fixed Layer meta, stored in files used to identify the layer.
data class LayerMeta(
    val title: String,
    val boundingBox: BoundingBox,
    val identifier: String,
    val styles: List<String>,
    val formats: List<TileFormat>
) {
    // We define custom serializers/deserializers since the XML's we store
    // are not valid, since they are not full (namespaces are absent).
    fun toXML(): String {
        val firstPart = """
            <Layer>
                <ows:Title>$title</ows:Title>
                <ows:WGS84BoundingBox>
                    <ows:LowerCorner>${boundingBox.minX} ${boundingBox.minY}</ows:LowerCorner>
                    <ows:UpperCorner>${boundingBox.maxX} ${boundingBox.maxY}</ows:UpperCorner>
                </ows:WGS84BoundingBox>
                <ows:Identifier>$identifier</ows:Identifier>
        """
        val stylesPart = if (styles.isNotEmpty())
            styles.joinToString("") { "<Style isDefault=\"true\">$it</Style>" }
        else
            "<Style isDefault=\"true\">_null</Style>"

        val formatsPart = formats.joinToString("") { "<Format>$it</Format>" }

        return "$firstPart$stylesPart$formatsPart".trimIndent()
    }

    companion object {
        fun fromXML(xml: String): LayerMeta {
            val title = xml.substringAfter("<ows:Title>").substringBefore("</ows:Title>")
            val minX = xml.substringAfter("<ows:LowerCorner>").substringBefore(" ").toDouble()
            val minY = xml.substringAfter("<ows:LowerCorner> ").substringBefore("</ows:LowerCorner>").toDouble()
            val maxX = xml.substringAfter("<ows:UpperCorner>").substringBefore(" ").toDouble()
            val maxY = xml.substringAfter("<ows:UpperCorner> ").substringBefore("</ows:UpperCorner>").toDouble()
            val identifier = xml.substringAfter("<ows:Identifier>").substringBefore("</ows:Identifier>")
            val styles = xml.substringAfter("<Style isDefault=\"true\">").substringBefore("</Style>")
                .split("</Style><Style isDefault=\"true\">")
                .map { it.replace("</Style>", "").replace("<Style isDefault=\"true\">", "") }
                .filter { it != "_null" }
            val formats = xml.substringAfter("<Format>").substringBefore("</Format>")
                .split("</Format><Format>")
                .map { it.replace("</Format>", "").replace("<Format>", "") }
                .map { TileFormat.fromMime(it)!! }

            return LayerMeta(title, BoundingBox(minX, minY, maxX, maxY), identifier, styles, formats)
        }
    }
}


@Serializable
data class TMSMeta(
    val identifier: String,
    val supportedCRS: String,
    val tileMatrixes: List<TileMatrixMeta>,
) {
    fun toXML(): String {
        val tileMatrixesPart = tileMatrixes.joinToString("") { it.toXML() }
        return """
            <TileMatrixSet>
                <ows:Identifier>$identifier</ows:Identifier>
                <ows:SupportedCRS>$supportedCRS</ows:SupportedCRS>
                $tileMatrixesPart
            </TileMatrixSet>
        """.trimIndent()
    }

    companion object {
        fun fromXML(xml: String): TMSMeta {
            val identifier = xml.substringAfter("<ows:Identifier>").substringBefore("</ows:Identifier>")
            val supportedCRS = xml.substringAfter("<ows:SupportedCRS>").substringBefore("</ows:SupportedCRS>")
            val tileMatrixes = xml.split("<TileMatrix>")
                .drop(1)
                .map { TileMatrixMeta.fromXML(it) }

            return TMSMeta(identifier, supportedCRS, tileMatrixes)
        }
    }
}

@Serializable
data class TileMatrixMeta(
    val identifier: String,
    val scaleDenominator: Double,
    val topLeftCorner: Pair<Double, Double>,
    val tileWidth: Int,
    val tileHeight: Int,
    val matrixWidth: Int,
    val matrixHeight: Int,
) {
    fun toXML(): String {
        return """
            <TileMatrix>
                <ows:Identifier>$identifier</ows:Identifier>
                <ScaleDenominator>$scaleDenominator</ScaleDenominator>
                <TopLeftCorner>${topLeftCorner.first} ${topLeftCorner.second}</TopLeftCorner>
                <TileWidth>$tileWidth</TileWidth>
                <TileHeight>$tileHeight</TileHeight>
                <MatrixWidth>$matrixWidth</MatrixWidth>
                <MatrixHeight>$matrixHeight</MatrixHeight>
            </TileMatrix>
        """.trimIndent()
    }

    companion object {
        fun fromXML(xml: String): TileMatrixMeta {
            val identifier = xml.substringAfter("<ows:Identifier>").substringBefore("</ows:Identifier>")
            val scaleDenominator = xml.substringAfter("<ScaleDenominator>").substringBefore("</ScaleDenominator>").toDouble()
            val topLeftCorner = xml.substringAfter("<TopLeftCorner>").substringBefore("</TopLeftCorner>")
                .split(" ")
                .map { it.toDouble() }
            val tileWidth = xml.substringAfter("<TileWidth>").substringBefore("</TileWidth>").toInt()
            val tileHeight = xml.substringAfter("<TileHeight>").substringBefore("</TileHeight>").toInt()
            val matrixWidth = xml.substringAfter("<MatrixWidth>").substringBefore("</MatrixWidth>").toInt()
            val matrixHeight = xml.substringAfter("<MatrixHeight>").substringBefore("</MatrixHeight>").toInt()

            return TileMatrixMeta(
                identifier,
                scaleDenominator,
                Pair(topLeftCorner[0], topLeftCorner[1]),
                tileWidth,
                tileHeight,
                matrixWidth,
                matrixHeight
            )
        }
    }
}