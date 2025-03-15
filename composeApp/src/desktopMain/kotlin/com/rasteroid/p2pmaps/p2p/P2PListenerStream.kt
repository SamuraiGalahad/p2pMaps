package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.tile.TileMeta
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.Socket


// 1. meta -> Long (0 if don't have, >0 for size)
// 2. meta, bytesCount (bytes requested) -> ByteArray
// 3. Unit -> List<meta>

class P2PListenerStream(
    private val peerSocket: Socket,
    private val log: Logger,
    val metaProvider: () -> List<RasterInfo>,
    val rasterQuery: (meta: TileMeta, bytesCount: Int) -> ByteArray
) {
    enum class State {
        NONE,
        DATA_TRANSFER,
        SENT_LAST_DATA,
        CLOSED
    }

    private var wantedMeta: TileMeta? = null
    private var state: State = State.NONE
    private var lastPacketReceivedTime: Long = 0

    fun listen() {
        while (state != State.CLOSED) {
            log.d("#${peerSocket.inetAddress} Waiting for messages from peer")
            // TODO: Create one large buffer and copy small buffers into it.
            // There can be messages that are very large.
            val (bytesRead, buffer) = receive(peerSocket, 1024)
            if (bytesRead == -1) {
                // Stream closed by the requester.
                log.d("#${peerSocket.inetAddress} Stream closed by peer")
                break
            }
            handleData(buffer.copyOf(bytesRead))
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun handleData(data: ByteArray) {
        lastPacketReceivedTime = System.currentTimeMillis()
        when (val message = ProtoBuf.decodeFromByteArray<Message>(data)) {
            is Message.Close -> {
                log.d("#${peerSocket.inetAddress} Received Closed")
                onCloseReceived()
            }
            is Message.Reply -> {
                log.d("#${peerSocket.inetAddress} Received Reply: ${message.reply}")
                onReplyReceived(message.reply)
            }
            is Message.Query -> {
                log.d("#${peerSocket.inetAddress} Received Query")
                onQueryReceived()
            }
            is Message.Have -> {
                log.d("#${peerSocket.inetAddress} Received Have: ${message.meta}")
                onHaveReceived(message.meta)
            }
            is Message.Want -> {
                log.d("#${peerSocket.inetAddress} Received Want: ${message.meta}")
                onWantReceived(message.meta)
            }
            is Message.StartData -> {
                log.w("#${peerSocket.inetAddress} " +
                        "Received unexpected StartData: ${message.dataSizeBytes}")
                onCloseReceived()
            } // not expected.
            is Message.Data -> {
                log.w("#${peerSocket.inetAddress} " +
                        "Received unexpected Data: ${message.data.size}")
                onCloseReceived()
            } // not expected.
            is Message.Metas -> {
                log.w("#${peerSocket.inetAddress} " +
                        "Received unexpected Metas: ${message.metas.size}")
                onCloseReceived()
            } // not expected.
        }
    }

    private fun onQueryReceived() {
        val metas = metaProvider().map { it.meta }
        send(peerSocket, Message.Metas(metas))
    }

    private fun onCloseReceived() {
        // Close message received from the requester, close stream.
        peerSocket.close()
        state = State.CLOSED
    }

    private fun onReplyReceived(reply: Boolean) {
        log.d("#${peerSocket.inetAddress} Received Reply: $reply")

        if (!reply) {
            onCloseReceived()
        }

        // Reply received from the requester, can reset the state.
        if (state == State.SENT_LAST_DATA) {
            wantedMeta = null
            state = State.NONE
            return
        }

        if (state != State.DATA_TRANSFER) {
            onCloseReceived()
        }

        // Send the next data packet.
        val data = rasterQuery(wantedMeta!!, 1024)
        if (data.size < 1024) {
            state = State.SENT_LAST_DATA
        }
        log.d("#${peerSocket.inetAddress} Sending next data packet")
        send(peerSocket, Message.Data(data))
    }

    private fun onHaveReceived(meta: TileMeta) {
        send(peerSocket, Message.Reply(metaProvider().any { it.meta == meta }))
    }

    private fun onWantReceived(meta: TileMeta) {
        val rasterSize = metaProvider().find { it.meta == meta }?.fileSize ?: 0L
        if (rasterSize == 0L) {
            send(peerSocket, Message.Reply(false))
            return
        }

        state = State.DATA_TRANSFER
        wantedMeta = meta
        send(peerSocket, Message.StartData(rasterSize))
    }
}