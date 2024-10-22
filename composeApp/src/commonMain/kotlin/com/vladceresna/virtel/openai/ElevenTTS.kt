package com.vladceresna.virtel.openai

import com.vladceresna.virtel.getHttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import okio.SYSTEM

fun toSpeech(text: String, key: String, filePath: String): String {
    runBlocking {

        val response = getHttpClient().post(
            "https://api.elevenlabs.io/v1/text-to-speech/AAf5O13lYHNKE7VJQL4x"){
            headers {
                append(HttpHeaders.Accept, "audio/mpeg")
                append("xi-api-key", key)
                append(HttpHeaders.ContentType, "application/json")
            }
            setBody("""
                {
                    "model_id": "eleven_multilingual_v2",
                    "voice_settings": {
                      "stability": 0.5,
                      "similarity_boost": 0.5
                    },
                    "text": "$text"
                }
            """.trimIndent())
        }
        println(response.status.toString())
        if (response.status == HttpStatusCode.OK) {
            okio.FileSystem.SYSTEM.createDirectories(filePath.toPath().parent!!)

            val body = response.bodyAsBytes()

            okio.FileSystem.SYSTEM.write(filePath.toPath()) { write(source = body) }

        }
    }
    return filePath
}