package com.vladceresna.virtel.controllers

import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import okio.SYSTEM

data object Launchers {
    fun loadLauncher(url: String) {
        println(url)
        CoroutineScope(Dispatchers.Main).launch {
            var result = Networker.download(url)
            log("Virtel will be installed on " +
                    com.vladceresna.virtel.controllers.FileSystem.launcherStartPath, Log.INFO)
            VarInstaller.install("vladceresna.virtel.launcher",result)
        }
    }
}