package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.getHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Url

object Networker {
    private var client = HttpClient()

    suspend fun download(url: String): String{
        client = getHttpClient()
        val response: HttpResponse = client.get(url)
        println(response.status)
        println(response.bodyAsText())
        client.close()
        return response.bodyAsText()
    }
}