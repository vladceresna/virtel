package com.vladceresna.virtel.controllers

import kotlinx.coroutines.runBlocking

data object Launchers {
    fun loadLauncher(url: String) {
        println(url)
        runBlocking {
            var result = Networker.download(url)
            log("Virtel will be installed on " +
                    com.vladceresna.virtel.controllers.FileSystem.launcherStartPath, Log.INFO)
            VarInstaller.install("vladceresna.virtel.launcher",result)
        }
    }
}