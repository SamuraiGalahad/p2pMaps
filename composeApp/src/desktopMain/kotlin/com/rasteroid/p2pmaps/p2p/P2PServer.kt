package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.server.TileRepository
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

fun listen(
    socket: DatagramSocket
) {
    try {
        val buffer = ByteArray(8192)
        val packet = DatagramPacket(buffer, buffer.size)
        while (true) {
            socket.receive(packet)
            handlePacket(socket, packet)
        }
    } catch (e: Exception) {
        log.e("Error listening for peers", e)
    } finally {
        socket.close()
        log.i("Stopped listening for peers")
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun handlePacket(socket: DatagramSocket, packet: DatagramPacket) {
    val message = ProtoBuf.decodeFromByteArray<Message>(packet.data)
    peerInfo(packet, "Received ${message.javaClass.simpleName}")
    when (message) {
        is Message.Ping -> handlePing(socket, packet)
        is Message.Layers -> handleLayers(socket, packet)
        is Message.Layer -> handleLayer(socket, packet, message)
        is Message.TileMatrixSet -> handleTileMatrixSet(socket, packet, message)
        is Message.TileSize -> handleTileSize(socket, packet, message)
        is Message.Tile -> handleTile(socket, packet, message)
        else -> unexpected(packet, message)
    }
}

private fun handlePing(
    socket: DatagramSocket,
    packet: DatagramPacket
) = send(socket, packet.address, packet.port, Message.Pong)

private fun handleLayers(
    socket: DatagramSocket,
    packet: DatagramPacket,
) {
    val rasters = TileRepository.instance.getLayerTMSs()
    send(socket, packet.address, packet.port, Message.LayersReply(rasters))
}

private fun handleTile(
    socket: DatagramSocket,
    packet: DatagramPacket,
    message: Message.Tile
) {
    val tile = TileRepository.instance.getTile(
        message.meta.layer,
        message.meta.tileMatrixSet,
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
        message.meta.layer,
        message.meta.tileMatrixSet,
        message.meta.tileMatrix,
        message.meta.tileCol,
        message.meta.tileRow,
        message.meta.format
    ) ?: 0
    send(socket, packet.address, packet.port, Message.TileSizeReply(tileSize))
}

private fun handleLayer(
    socket: DatagramSocket,
    packet: DatagramPacket,
    message: Message.Layer
) {
    val layerInfo = TileRepository.instance.getLayerInfo(message.layer)
    send(socket, packet.address, packet.port, Message.LayerReply(layerInfo))
}

private fun handleTileMatrixSet(
    socket: DatagramSocket,
    packet: DatagramPacket,
    message: Message.TileMatrixSet
) {
    val tileMatrixSet = TileRepository.instance.getTMSMeta(
        message.tileMatrixSet
    )
    send(socket, packet.address, packet.port, Message.TileMatrixSetReply(tileMatrixSet))
}

private fun unexpected(
    packet: DatagramPacket,
    message: Message
) {
    peerWarning(packet, "Unexpected message: ${message.javaClass.simpleName}")
}



