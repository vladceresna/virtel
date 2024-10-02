package com.vladceresna.virtel

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.io.File

actual fun isFileExists(path: String): Boolean = File(path).exists()
actual fun getHttpClient(): HttpClient {
    return HttpClient(OkHttp)
}