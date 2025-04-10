package com.rasteroid.p2pmaps.tile

enum class TileFormat {
    PNG,
    JPEG;

    companion object {
        fun fromMime(mime: String): TileFormat? = when (mime) {
            "image/png" -> PNG
            "image/jpeg" -> JPEG
            else -> null
        }
    }

    fun getMime(): String = when (this) {
        PNG -> "image/png"
        JPEG -> "image/jpeg"
    }

    fun getExtension(): String = when (this) {
        PNG -> "png"
        JPEG -> "jpg"
    }
}