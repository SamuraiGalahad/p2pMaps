package com.rasteroid.p2pmaps.p2p

import kotlinx.coroutines.withTimeout
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

@OptIn(ExperimentalSerializationApi::class)
fun send(
    socket: DatagramSocket,
    address: InetAddress,
    port: Int,
    message: Message
) {
    val data = ProtoBuf.encodeToByteArray(message)
    val packet = DatagramPacket(data, data.size, address, port)
    socket.send(packet)
}

fun receive(
    socket: DatagramSocket,
    bytes: Int
): Pair<Int, ByteArray> {
    val buffer = ByteArray(bytes)
    val packet = DatagramPacket(buffer, buffer.size)
    // Blocking.
    // TODO: Add timeouts.
    socket.receive(packet)
    return packet.length to buffer
}
