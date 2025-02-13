package com.rasteroid.p2pmaps

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import java.time.LocalDateTime

data class Log(
    // proper time date value.
    val time: LocalDateTime,
    val severity: Severity,
    val tag: String,
    val message: String,
    val throwable: Throwable?
)

class LastNLogs(private val logsCount: Int) : LogWriter() {
    private val _logs = mutableStateListOf<Log>()
    val logs: SnapshotStateList<Log> get() = _logs

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        _logs.add(Log(LocalDateTime.now(), severity, tag, message, throwable))
        if (logs.size > logsCount) {
            _logs.removeAt(0)
        }
    }
}

val lastLogs = LastNLogs(100)
