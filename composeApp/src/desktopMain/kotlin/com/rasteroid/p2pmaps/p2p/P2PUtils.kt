package com.rasteroid.p2pmaps.p2p

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.Socket

@OptIn(ExperimentalSerializationApi::class)
fun send(socket: Socket, message: Message) {
    val data = ProtoBuf.encodeToByteArray(message)
    socket.getOutputStream().write(data)
}

fun receive(socket: Socket, bytes: Int): Pair<Int, ByteArray> {
    val buffer = ByteArray(bytes)
    val bytesRead = socket.getInputStream().read(buffer)
    return Pair(bytesRead, buffer)
}
