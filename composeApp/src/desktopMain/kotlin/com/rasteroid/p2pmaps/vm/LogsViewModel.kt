package com.rasteroid.p2pmaps.vm

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.rasteroid.p2pmaps.Log
import com.rasteroid.p2pmaps.lastLogs
import java.time.format.DateTimeFormatter

class LogsViewModel : ViewModel() {
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
    val logs: SnapshotStateList<Log> get() = lastLogs.logs
}