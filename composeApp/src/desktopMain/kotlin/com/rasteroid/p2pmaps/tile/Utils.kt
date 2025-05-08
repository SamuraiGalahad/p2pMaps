package com.rasteroid.p2pmaps.tile

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.math.roundToLong
import kotlin.system.measureTimeMillis

data class InternetSpeedTestResult(
    val downloadSpeed: Long,
    val uploadSpeed: Long
)

suspend fun getInternetSpeedTest(
    httpClient: HttpClient,
    downloadUrl: String,
    uploadUrl: String
): Result<InternetSpeedTestResult> {
    val downloadResult = getInternetDownloadSpeed(httpClient, downloadUrl)
    if (downloadResult.isFailure) {
        return Result.failure(downloadResult.exceptionOrNull() ?: Exception("Unknown error"))
    }

    val uploadResult = getInternetUploadSpeed(httpClient, uploadUrl)
    if (uploadResult.isFailure) {
        return Result.failure(uploadResult.exceptionOrNull() ?: Exception("Unknown error"))
    }

    val downloadSpeed = downloadResult.getOrThrow()
    val uploadSpeed = uploadResult.getOrThrow()
    return Result.success(InternetSpeedTestResult(downloadSpeed, uploadSpeed))
}

private suspend fun getInternetDownloadSpeed(
    httpClient: HttpClient,
    downloadUrl: String
): Result<Long> {
    return runCatching {
        val downloadSizeMb = 10
        val downloadTime = measureTimeMillis {
            httpClient.get(downloadUrl) {
                parameter("ckSize", downloadSizeMb) // 10Mb to download.
            }
        }
        val speed = (downloadSizeMb * 1024 * 1024 * 8L) / (downloadTime * 0.001) // in bits per second
        return@runCatching speed.roundToLong()
    }
}

private suspend fun getInternetUploadSpeed(
    httpClient: HttpClient,
    uploadUrl: String
): Result<Long> {
    return runCatching {
        val uploadSize = 10 * 1024 * 1024
        val data = ByteArray(uploadSize) { 0 }
        val uploadTime = measureTimeMillis {
            httpClient.post(uploadUrl) {
                contentType(ContentType.Application.OctetStream)
                setBody(data)
            }
        }
        val speed = (uploadSize * 8L) / (uploadTime * 0.001) // in bits per second
        return@runCatching speed.roundToLong()
    }
}