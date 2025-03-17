package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.RasterMeta
import com.rasteroid.p2pmaps.tile.TileMeta
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.DatagramSocket
import java.net.InetAddress

private val log = Logger.withTag("p2p request")

fun getSocketOnAnyAvailablePort(): DatagramSocket = DatagramSocket(0)

fun requestRasters(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int
) = sendAndWaitForReply<Message.RastersReply>(socket, address, port, Message.Rasters)

fun requestTile(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    meta: TileMeta
): Result<ByteArray> {
    // Request tile size first.
    val tileSize = requestTileSize(
        socket,
        address,
        port,
        meta
    ).getOrElse { return Result.failure(it) }

    // Sequentially request tile by chunks of 4096 bytes.
    val tile = ByteArray(tileSize.dataSizeBytes)
    var offset = 0
    while (offset < tile.size) {
        val chunkSize = minOf(4096, tile.size - offset)
        val reply = sendAndWaitForReply<Message.TileReply>(
            socket,
            address,
            port,
            Message.Tile(meta, offset, chunkSize)
        ).getOrElse { return Result.failure(it) }
        reply.tile.copyInto(tile, offset)
        offset += chunkSize
    }

    return Result.success(tile)
}

private fun requestTileSize(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    meta: TileMeta
) = sendAndWaitForReply<Message.TileSizeReply>(
    socket,
    address,
    port,
    Message.TileSize(meta)
)

fun requestDescribe(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    rasterMeta: RasterMeta
) = sendAndWaitForReply<Message.DescribeReply>(
    socket,
    address,
    port,
    Message.Describe(rasterMeta)
)

private inline fun <reified ReplyType : Message> sendAndWaitForReply(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    message: Message
): Result<ReplyType> {
    send(socket, address, port, message)
    return waitForReply(socket)
}

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified ReplyType : Message> waitForReply(
    socket: DatagramSocket,
): Result<ReplyType> {
    // TODO: Add timeouts.
    val (bytesRead, buffer) = receive(socket, 8192)
    if (bytesRead <= 0) return Result.failure(Exception("Peer unexpectedly closed connection."))
    val reply = ProtoBuf.decodeFromByteArray<Message>(buffer.copyOf(bytesRead))
    if (reply !is ReplyType) return Result.failure(Exception("Unexpected message received from peer: $reply"))
    return Result.success(reply)
}
