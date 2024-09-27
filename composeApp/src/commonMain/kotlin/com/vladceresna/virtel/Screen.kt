package com.vladceresna.virtel

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

@Composable
fun Screen(appId: String) {
    Text("Hello")

        embeddedServer(CIO, port = 8080) {
            routing {
                get("/") {
                    call.respondText(text = "Hello world!")
                }
            }
        }.start(wait = true)

}
