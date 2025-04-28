package com.rasteroid.p2pmaps.server

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.TileFormat
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

private val log = Logger.withTag("WMTS Server")

class WMTSServer(
    port: Int,
    prefix: String,
    private val tileRepository: TileRepository
) {
    private val server = embeddedServer(Netty, port = port) {
        routing {
            get(prefix) {
                handleRequest(call)
            }
        }
    }

    fun start() {
        server.start(wait = false)
    }

    fun stop() {
        server.stop(1000, 1000)
    }

    private fun getWMTSCapabilitiesTemplate(): String {
        val stream = Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream("wmts-capabilities-template.xml")!!
        // Template has empty <contents> tag, we need to add contents from the repository.
        val contents = tileRepository.getContents()
        return stream.bufferedReader().use { it.readText().replace("{{ contents }}", contents) }
    }

    private suspend fun handleRequest(call: ApplicationCall) {
        val service = call.request.queryParameters["service"]?.lowercase()
        if (service == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing service parameter")
            return
        }

        if (service != "wmts") {
            call.respond(HttpStatusCode.BadRequest, "Unsupported service (expected WMTS)")
            return
        }

        val request = call.request.queryParameters["request"]?.lowercase()
        if (request == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing request parameter")
            return
        }

        when (request) {
            "getcapabilities" -> handleCapabilitiesRequest(call)
            "gettile" -> handleTileRequest(call)
            else -> call.respond(
                HttpStatusCode.BadRequest,
                "Unsupported request (expected GetCapabilities or GetTile)"
            )
        }
    }

    private suspend fun handleCapabilitiesRequest(call: ApplicationCall) {
        val capabilities = getWMTSCapabilitiesTemplate()
        call.respondText(contentType = ContentType.Application.Xml, text = capabilities)
    }

    private suspend fun handleTileRequest(call: ApplicationCall) {
        val layer = call.request.queryParameters["layer"]
        if (layer == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing layer parameter")
            return
        }

        val tileMatrixSet = call.request.queryParameters["TileMatrixSet"]
        if (tileMatrixSet == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing TileMatrixSet parameter")
            return
        }

        val tileMatrix = call.request.queryParameters["TileMatrix"]
        if (tileMatrix == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing TileMatrix parameter")
            return
        }

        val tileCol = call.request.queryParameters["TileCol"]?.toIntOrNull()
        if (tileCol == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing TileCol parameter")
            return
        }

        val tileRow = call.request.queryParameters["TileRow"]?.toIntOrNull()
        if (tileRow == null) {
            call.respond(HttpStatusCode.BadRequest, "Missing TileRow parameter")
            return
        }

        val format = TileFormat.fromMime(call.request.queryParameters["Format"]?.lowercase() ?: "")
        if (format == null) {
            call.respond(HttpStatusCode.BadRequest, "Unsupported format")
            return
        }

        log.d("Received tile request: " +
                "layer=$layer, " +
                "tileMatrixSet=$tileMatrixSet, " +
                "TileMatrix=$tileMatrix, " +
                "TileCol=$tileCol, " +
                "TileRow=$tileRow, " +
                "format=${format.getMime()}"
        )

        val tile = tileRepository.getTile(
            layer = layer,
            tileMatrixSet = tileMatrixSet,
            tileMatrix = tileMatrix,
            tileCol = tileCol,
            tileRow = tileRow,
            format = format
        )

        if (tile != null) {
            call.respondBytes(contentType = ContentType.parse(format.getMime()), bytes = tile)
        } else {
            call.respond(HttpStatusCode.NotFound, "Tile not found")
        }
    }
}