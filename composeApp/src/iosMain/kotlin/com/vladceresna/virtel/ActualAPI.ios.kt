package com.vladceresna.virtel

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun getHttpClient(): HttpClient {
    return HttpClient(Darwin)
}
actual fun openSettings() {}

actual fun openApp(packageName: String) {}