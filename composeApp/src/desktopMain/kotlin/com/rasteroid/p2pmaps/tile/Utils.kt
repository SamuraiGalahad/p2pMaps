package com.rasteroid.p2pmaps.tile

import co.touchlab.kermit.Logger
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import kotlinx.coroutines.delay

private val logger = Logger.withTag("tile utils")

data class InternetSpeedTestResult(
    val downloadSpeed: Long,
    val uploadSpeed: Long
)

private class SimpleSpeedTestListener : ISpeedTestListener {
    var isCompleted = false
        private set

    var isError = false
        private set

    var transferRateBit = -1L
        private set

    override fun onCompletion(report: SpeedTestReport?) {
        if (report == null) return
        transferRateBit = report.transferRateBit.toLong()
        isCompleted = true
    }

    override fun onProgress(percent: Float, report: SpeedTestReport?) {}

    override fun onError(speedTestError: SpeedTestError?, errorMessage: String?) {
        isError = true
        isCompleted = true
    }
}

suspend fun getInternetSpeedTest(): InternetSpeedTestResult {
    val downloadSocket = SpeedTestSocket()
    val uploadSocket = SpeedTestSocket()

    val downloadListener = SimpleSpeedTestListener()
    val uploadListener = SimpleSpeedTestListener()

    downloadSocket.addSpeedTestListener(downloadListener)
    uploadSocket.addSpeedTestListener(uploadListener)

    // TODO: Maybe do something about hardcoding URLs, like moving them to config.
    downloadSocket.startDownload("http://msk3.companion.t2.ru:8080/speedtest/random2000x2000.jpg")
    uploadSocket.startUpload("http://msk3.companion.t2.ru:8080/speedtest/upload.php", 1_000_000)

    while (!downloadListener.isCompleted || !uploadListener.isCompleted) {
        delay(100)
    }

    var downloadSpeed = 0L
    var uploadSpeed = 0L

    if (!downloadListener.isError) {
        downloadSpeed = downloadListener.transferRateBit
    } else {
        logger.e("Testing internet download speed failed")
    }

    if (!uploadListener.isError) {
        uploadSpeed = uploadListener.transferRateBit
    } else {
        logger.e("Testing internet upload speed failed")
    }

    downloadSocket.closeSocket()
    uploadSocket.closeSocket()

    return InternetSpeedTestResult(
        downloadSpeed = downloadSpeed,
        uploadSpeed = uploadSpeed
    )
}