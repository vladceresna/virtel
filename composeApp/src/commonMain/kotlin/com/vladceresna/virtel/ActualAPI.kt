package com.vladceresna.virtel

import io.ktor.client.HttpClient

expect fun getHttpClient():HttpClient

expect fun openSettings()

expect fun openApp(packageName: String)