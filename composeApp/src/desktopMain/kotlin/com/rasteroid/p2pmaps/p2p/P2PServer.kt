package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.RasterReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket

// Raw sockets for now, probably switching to ktor later.

private val log = Logger.withTag("p2p listen")

suspend fun listen(
    port: Int,
) = withContext(Dispatchers.IO) {
    val serverSocket = ServerSocket(port)
    log.i("Listening on port $port for peers")
    try {
        while (true) {
            val socket = serverSocket.accept()
            log.i("Accepted peer connection from ${socket.inetAddress}")
            val reader = RasterReader()
            val stream = P2PListenerStream(
                peerSocket = socket,
                log = log,
                metaProvider = { InternalRasterRepository.instance.rasterInfos },
                rasterQuery = { meta, bytesCount ->
                    // some random byte array of size bytesCount for now.
                    reader.readNextChunk(meta, bytesCount)
                }
            )
            launch {
                stream.listen()
            }
        }
    } catch (e: Exception) {
        log.e("Error listening for peers", e)
    } finally {
        serverSocket.close()
        log.i("Stopped listening for peers")
    }
}