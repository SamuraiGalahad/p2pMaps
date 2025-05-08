package com.rasteroid.p2pmaps.server.dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackerCheckReply(
    val host: String,
    val port: Int,
    val key: String
)

@Serializable
data class TrackerAskReply(
    val host: String,
    val port: Int,
    val key: String,
    val tiles: List<TrackerAskReplyTile>
)

@Serializable
data class TrackerAskReplyTile(
    val tileMatrix: String,
    val tileColsAndRows: List<List<Int>>,
    val format: String
)
