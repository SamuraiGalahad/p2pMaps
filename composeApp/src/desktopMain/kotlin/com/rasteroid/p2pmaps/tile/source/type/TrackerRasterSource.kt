package com.rasteroid.p2pmaps.tile.source.type

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.server.TileRepository
import com.rasteroid.p2pmaps.tile.RasterMeta
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.io.OutputStream

private val log = Logger.withTag("tracker raster source")

class TrackerRasterSource(
    private val remoteUrl: String
) : RasterSource {
    override val name: String = "Tracker"
    override val type: RasterSourceType = RasterSourceType.PEER

    val client = HttpClient {
        install(ContentNegotiation) {
            json( Json { ignoreUnknownKeys = true })
        }
    }

    fun announce() {
        client.post("$remoteUrl/announce") {
            body = TileRepository.announce()
        }
    }

    override fun fetch(onLayerFound: (RasterMeta) -> Unit) {
        //
    }

    override fun download(
        resultStream: OutputStream,
        rasterMeta: RasterMeta,
        onDataStart: (Long) -> Unit
    ) {
        log.d("Not implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TrackerRasterSource

        if (remoteUrl != other.remoteUrl) return false
        if (name != other.name) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = remoteUrl.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}