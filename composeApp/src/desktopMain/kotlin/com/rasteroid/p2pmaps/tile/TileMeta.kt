package com.rasteroid.p2pmaps.tile

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class BoundingBox(
    val minX: Double,
    val minY: Double,
    val maxX: Double,
    val maxY: Double
)

@Serializable
data class RasterMeta(
    @SerialName("NAME")
    val layer: String,
    @SerialName("TYPE")
    val tileMatrixSet: String
)

@Serializable
data class TileMeta(
    val rasterMeta: RasterMeta,
    val tileMatrix: String,
    val tileCol: Int,
    val tileRow: Int,
    val format: RasterFormat
)

// The reply for Message.Rasters call.
// Additionally, XML-encoded meta for each layer in info.xml.
@Serializable
data class RasterReply(
    @XmlSerialName("ows:Title")
    val title: String,
    @XmlSerialName("ows:WGS84BoundingBox")
    val boundingBox: BoundingBox,
    @XmlSerialName("ows:Identifier")
    val identifier: String,
    @XmlSerialName("Style")
    val styles: List<LayerStyle>,
    @XmlSerialName("Format")
    val formats: List<RasterFormat>
)

@Serializable
data class LayerStyle(
    @XmlSerialName("isDefault")
    @XmlValue(false) // Making this an attribute rather than a tag.
    val isDefault: Boolean = true,
    @XmlSerialName("ows:Identifier")
    val identifier: String = "_null"
)

@Serializable
data class TileMatrixSet(
    @XmlSerialName("ows:Identifier")
    val identifier: String,
    @XmlSerialName("ows:SupportedCRS")
    val supportedCRS: String,
    @XmlSerialName("TileMatrix")
    val tileMatrix: List<TileMatrix>
)

@Serializable
data class TileMatrix(
    @XmlSerialName("ows:Identifier")
    val identifier: String,
    @XmlSerialName("ScaleDenominator")
    val scaleDenominator: Double,
    @XmlSerialName("TopLeftCorner")
    val topLeftCorner: Pair<Double, Double>,
    @XmlSerialName("TileWidth")
    val tileWidth: Int,
    @XmlSerialName("TileHeight")
    val tileHeight: Int,
    @XmlSerialName("MatrixWidth")
    val matrixWidth: Int,
    @XmlSerialName("MatrixHeight")
    val matrixHeight: Int
)