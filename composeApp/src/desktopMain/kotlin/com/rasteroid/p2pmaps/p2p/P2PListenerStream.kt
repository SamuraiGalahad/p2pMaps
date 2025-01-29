package com.rasteroid.p2pmaps.p2p

import co.touchlab.kermit.Logger
import com.rasteroid.p2pmaps.raster.RasterMeta
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.Socket

class P2PListenerStream(
    private val peerSocket: Socket,
    private val log: Logger,
    val isRasterAvailable: (meta: RasterMeta) -> Long,
    val rasterQuery: (meta: RasterMeta, bytesCount: Int) -> ByteArray
) {
    enum class State {
        NONE,
        DATA_TRANSFER,
        SENT_LAST_DATA,
        CLOSED
    }

    private var wantedMeta: RasterMeta? = null
    private var state: State = State.NONE
    private var lastPacketReceivedTime: Long = 0

    fun listen() {
        while (state != State.CLOSED) {
            log.d("#${peerSocket.inetAddress} Waiting for messages from peer")
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
        }
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

    private fun onHaveReceived(meta: RasterMeta) {
        send(peerSocket, Message.Reply(isRasterAvailable(meta) > 0L))
    }

    private fun onWantReceived(meta: RasterMeta) {
        val rasterSize = isRasterAvailable(meta)
        if (rasterSize == 0L) {
            send(peerSocket, Message.Reply(false))
            return
        }

        state = State.DATA_TRANSFER
        wantedMeta = meta
        send(peerSocket, Message.StartData(rasterSize))
    }
}