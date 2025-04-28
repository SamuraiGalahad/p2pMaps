package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.TileMeta
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException

private const val SOCKET_TIMEOUT_MILLIS = 15 * 1000

private val log = Logger.withTag("p2p request")

fun getSocketOnAnyAvailablePort(timeout: Int = SOCKET_TIMEOUT_MILLIS): DatagramSocket {
    val socket = DatagramSocket(0)
    socket.soTimeout = timeout
    return socket
}

fun udpHolePunch(
    connectionKey: String,
    peerDiscoveryUrl: String,
    peerDiscoveryPort: Int
): Result<Triple<DatagramSocket, InetAddress, Int>> {
    // Contacting discovery UDP socket on tracker
    // and trying to establish a connection to a peer by UDP hole punching.
    val socket = getSocketOnAnyAvailablePort()
    val buffer = connectionKey.toByteArray()
    val address = InetAddress.getByName(peerDiscoveryUrl)
    val packet = DatagramPacket(buffer, buffer.size, address, peerDiscoveryPort)
    val receivedPacket = DatagramPacket(ByteArray(1024), 1024)

    // Send and wait for peer address reply from tracker.
    log.d("Starting UDP Hole punch. key: $connectionKey address: $address port: $peerDiscoveryPort")
    for (attempt in 1..5) {
        try {
            socket.send(packet)
            socket.receive(receivedPacket)
            break
        } catch (e: SocketTimeoutException) {
            log.d("Timed out waiting for other peer while hole punching, reattempting...")
            if (attempt == 5) {
                log.e("Failed to receive peer address after 5 attempts.")
                return Result.failure(e)
            }
        }
    }

    // Extract address and port from the received packet
    // in the form of "address:port".
    val peerAddressPortRaw = String(receivedPacket.data, 0, receivedPacket.length)
    log.d("peerAddressPort: $peerAddressPortRaw")
    val peerAddressPort = peerAddressPortRaw.split(":")
        .map { it.trim() }

    val peerPort = peerAddressPort[1].toInt()
    val peerAddress = InetAddress.getByName(peerAddressPort[0])

    // Send a few packets to peer to try to punch through the NAT.
    for (i in 0 until 5) {
        sendAndWaitForReply<Message.Pong>(socket, peerAddress, peerPort, Message.Ping)
    }

    return Result.success(Triple(socket, peerAddress, peerPort))
}

fun requestLayersTMS(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int
) = sendAndWaitForReply<Message.LayersReply>(socket, address, port, Message.Layers)

fun requestLayer(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    layer: String
) = sendAndWaitForReply<Message.LayerReply>(
    socket,
    address,
    port,
    Message.Layer(layer)
)

fun requestTileMatrixSet(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    tileMatrixSet: String
) = sendAndWaitForReply<Message.TileMatrixSetReply>(
    socket,
    address,
    port,
    Message.TileMatrixSet(tileMatrixSet)
)

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
