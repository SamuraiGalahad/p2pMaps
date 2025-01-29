package com.rasteroid.p2pmaps.raster

import com.rasteroid.p2pmaps.raster.meta.RasterInfoRepository
import java.io.FileInputStream

class RasterReader {
    private var fileStream: FileInputStream? = null

    fun readNextChunk(meta: RasterMeta, bytes: Int): ByteArray {
        if (fileStream == null) {
            val rasterPath = RasterInfoRepository.instance.getRasterPath(meta)
            // TODO: error handling
            fileStream = FileInputStream(rasterPath.toString())
        }
        val buffer = ByteArray(bytes)
        fileStream!!.read(buffer)
        return buffer
    }
}
