package com.vladceresna.virtel.controllers

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url

object Networker {
    private var client = HttpClient()

    suspend fun download(url: Url): String{
        client = HttpClient()
        val response: HttpResponse = client.get(url)
        println(response.status)
        println(response.content)
        client.close()
        return response.content.toString()
    }
}