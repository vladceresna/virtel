package com.vladceresna.virtel.controllers

import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

data object Launchers {
    fun loadLauncher(url: String) {
        var result = ""
        CoroutineScope(Job()).launch {
            result = Networker.download(Url(url))
            println(result)
        }
        println(result)
        FileSystem.SYSTEM.write(
            com.vladceresna.virtel.controllers.FileSystem.launcherStartPath.toPath()
        ){writeUtf8(result)}
    }
}