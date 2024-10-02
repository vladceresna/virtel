package com.vladceresna.virtel

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun isFileExists(path: String): Boolean = false //TODO
actual fun getHttpClient(): HttpClient {
    return HttpClient(Darwin)
}