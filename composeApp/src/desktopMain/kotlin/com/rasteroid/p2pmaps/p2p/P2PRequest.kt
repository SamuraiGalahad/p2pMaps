package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.TileMeta
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.ConnectException
import java.net.Socket

private val log = Logger.withTag("p2p request")

fun requestRaster(
    meta: TileMeta,
    peerHost: String,
    peerPort: Int,
    onDataStart: (Long) -> Unit,
    onDataReceived: (ByteArray) -> Unit,
): Result<Unit> = runCatching {
        val socket = Socket(peerHost, peerPort)
        socket.use {
            // Check raster availability.
            log.i("#${socket.inetAddress} Checking if peer has raster $meta")
            sendAndWaitForReply<Message.Reply>(socket, Message.Have(meta))
                .onFailure { return Result.failure(it) }
                .onSuccess {
                    if (!it.reply)
                        return Result.failure(Exception("Peer does not have the requested raster."))
                }

            // Request raster.
            log.i("#${socket.inetAddress} Requesting raster $meta from peer")
            val startData = sendAndWaitForReply<Message.StartData>(socket, Message.Want(meta))
                .getOrElse { return Result.failure(it) }

            onDataStart(startData.dataSizeBytes)

            // Receive raster.
            log.i("#${socket.inetAddress} Started receiving raster $meta from peer")
            var receivedBytes = 0L
            while (receivedBytes < startData.dataSizeBytes) {
                log.d("#${socket.inetAddress} Waiting for next data chunk")
                val data = waitForReply<Message.Data>(socket)
                    .getOrElse { return Result.failure(it) }

                onDataReceived(data.data)
                receivedBytes += data.data.size
                log.d(
                    "#${socket.inetAddress} Received ${data.data.size} bytes, " +
                            "$receivedBytes/${startData.dataSizeBytes}"
                )

                // Reply to signal readiness to receive another chunk.
                send(socket, Message.Reply(true))
            }

            // Close connection.
            log.i("#${socket.inetAddress} Finished receiving raster $meta from peer")
            send(socket, Message.Close)
        }
    }.onFailure {
        when (it) {
            is ConnectException -> log.d("#$peerHost:$peerPort Connection refused")
            else -> log.w("#$peerHost:$peerPort Error requesting raster $meta from peer", it)
        }
    }

fun requestMetas(
    peerHost: String,
    peerPort: Int,
): Result<List<TileMeta>> = runCatching {
        val socket = Socket(peerHost, peerPort)
        socket.use {
            log.i("#${socket.inetAddress} Requesting raster metas from peer")
            val metas = sendAndWaitForReply<Message.Metas>(socket, Message.Query)
                .getOrElse { return Result.failure(it) }
            log.i("#${socket.inetAddress} Received ${metas.metas.size} raster metas from peer")
            metas.metas
        }
    }.onFailure {
        when (it) {
            is ConnectException -> log.d("#$peerHost:$peerPort Connection refused")
            else -> log.w("#$peerHost:$peerPort Error requesting raster metas from peer", it)
        }
    }

private inline fun <reified ReplyType : Message> sendAndWaitForReply(
    socket: Socket,
    message: Message,
): Result<ReplyType> {
    send(socket, message)
    return waitForReply(socket)
}

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified ReplyType : Message> waitForReply(
    socket: Socket,
): Result<ReplyType> {
    // TODO: Add timeouts.
    val (bytesRead, buffer) = receive(socket, 1024)
    if (bytesRead <= 0) return Result.failure(Exception("Peer unexpectedly closed connection."))
    val reply = ProtoBuf.decodeFromByteArray<Message>(buffer.copyOf(bytesRead))
    if (reply is Message.Close) return Result.failure(Exception("Peer unexpectedly closed connection."))
    if (reply !is ReplyType) return Result.failure(Exception("Unexpected message received from peer: $reply"))
    return Result.success(reply)
}
