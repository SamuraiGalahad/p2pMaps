package com.rasteroid.p2pmaps.raster

class LocalRasterSource : RasterSource {
    override val name: String = "Local"
    override val type: RasterSourceType = RasterSourceType.LOCAL

    override suspend fun fetchRasters(): List<RasterMeta> {
        // generate 10 random test raster metas.
        return (0 until 100).map {
            RasterMeta(
                format = "image/png",
                width = (1000..10000).random(),
                height = (1000..10000).random(),
                layers = listOf("Layer $it"),
                time = "2021-01-01",
                boundingBox = BoundingBox(0.0, 0.0, 0.0, 0.0),
            )
        }
    }

    override suspend fun downloadRaster(raster: RasterMeta) {
        // It is a raster source,
        // but it's not like we can "locally" download a raster.
    }
}