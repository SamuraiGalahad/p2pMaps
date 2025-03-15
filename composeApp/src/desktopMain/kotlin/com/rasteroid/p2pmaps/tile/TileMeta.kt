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
data class RasterMeta(
    val layer: String,
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

@Serializable
data class LayerMeta(
    val title: String,
    val boundingBox: BoundingBox,
    val identifier: String,
    val formats: List<RasterFormat>,
    val tileMatrixSets: List<String>
) {
    fun toLayerDescription(): String {
        // Simply transforming layer meta to WMTS format.
        val sb = StringBuilder("<Layer>\n")
        sb.append("  <Title>").append(title).append("</Title>\n")
        sb.append("  <ows:WGS84BoundingBox>\n")
        sb.append("    <ows:LowerCorner>").append(boundingBox.minX).append(" ").append(boundingBox.minY).append("</ows:LowerCorner>\n")
        sb.append("    <ows:UpperCorner>").append(boundingBox.maxX).append(" ").append(boundingBox.maxY).append("</ows:UpperCorner>\n")
        sb.append("  </ows:WGS84BoundingBox>\n")
        sb.append("  <ows:Identifier>").append(identifier).append("</ows:Identifier>\n")
        sb.append("  <Style isDefault=\"true\">\n")
        sb.append("    <ows:Identifier>default</ows:Identifier>\n")
        sb.append("  </Style>\n")
        for (format in formats) {
            sb.append("  <Format>").append(format.getMime()).append("</Format>\n")
        }
        for (tileMatrixSet in tileMatrixSets) {
            sb.append("  <TileMatrixSetLink>\n")
            sb.append("    <TileMatrixSet>").append(tileMatrixSet).append("</TileMatrixSet>\n")
            sb.append("  </TileMatrixSetLink>\n")
        }
        sb.append("</Layer>\n")
        return sb.toString()
    }
}

@Serializable
data class
