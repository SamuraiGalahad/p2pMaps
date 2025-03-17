package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.server.TileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.DatagramPacket
import java.net.DatagramSocket

private val log = Logger.withTag("p2p listen")

private fun peerInfo(
    packet: DatagramPacket,
    text: String
) = log.i("${packet.address}:${packet.port} $text")

private fun peerWarning(
    packet: DatagramPacket,
    text: String
) = log.w("${packet.address}:${packet.port} $text")

suspend fun listen(
    port: Int,
) = withContext(Dispatchers.IO) {
    val serverSocket = DatagramSocket(port)
    log.i("Listening on port $port for peers")
    try {
        val buffer = ByteArray(8192)
        val packet = DatagramPacket(buffer, buffer.size)
        while (true) {
            serverSocket.receive(packet)
            handlePacket(serverSocket, packet)
        }
    } catch (e: Exception) {
        log.e("Error listening for peers", e)
    } finally {
        serverSocket.close()
        log.i("Stopped listening for peers")
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun handlePacket(socket: DatagramSocket, packet: DatagramPacket) {
    val message = ProtoBuf.decodeFromByteArray<Message>(packet.data)
    peerInfo(packet, "Received ${message.javaClass.simpleName}")
    when (message) {
        is Message.Rasters -> handleRasters(socket, packet)
        is Message.Tile -> handleTile(socket, packet, message)
        is Message.TileSize -> handleTileSize(socket, packet, message)
        is Message.Describe -> handleDescribe(socket, packet, message)
        else -> unexpected(packet, message)
    }
}

private fun handleRasters(
    socket: DatagramSocket,
    packet: DatagramPacket,
) {
    val rasters = TileRepository.instance.getRasters()
    send(socket, packet.address, packet.port, Message.RastersReply(rasters))
}

private fun handleTile(
    socket: DatagramSocket,
    packet: DatagramPacket,
    message: Message.Tile
) {
    val tile = TileRepository.instance.getTile(
        message.meta.rasterMeta.layer,
        message.meta.rasterMeta.tileMatrixSet,
        message.meta.tileMatrix,
        message.meta.tileCol,
        message.meta.tileRow,
        message.meta.format,
        message.offsetBytes
    ) ?: ByteArray(0)
    send(socket, packet.address, packet.port, Message.TileReply(tile))
}

private fun handleTileSize(
    socket: DatagramSocket,
    packet: DatagramPacket,
    message: Message.TileSize
) {
    val tileSize = TileRepository.instance.getTileSize(
        message.meta.rasterMeta.layer,
        message.meta.rasterMeta.tileMatrixSet,
        message.meta.tileMatrix,
        message.meta.tileCol,
        message.meta.tileRow,
        message.meta.format
    ) ?: 0
    send(socket, packet.address, packet.port, Message.TileSizeReply(tileSize))
}

private fun handleDescribe(
    socket: DatagramSocket,
    packet: DatagramPacket,
    message: Message.Describe
) {
    val tileMatrixSet = TileRepository.instance.getTileMatrixSet(
        message.raster.layer,
        message.raster.tileMatrixSet
    )
    send(socket, packet.address, packet.port, Message.DescribeReply(tileMatrixSet))
}

private fun unexpected(
    packet: DatagramPacket,
    message: Message
) {
    peerWarning(packet, "Unexpected message: ${message.javaClass.simpleName}")
}



