package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.getHomePath
import com.vladceresna.virtel.other.VirtelException
import kotlinx.coroutines.delay

data object VirtelSystem {
    var isLoading: Boolean = true
    lateinit var renderFunction: () -> Unit

    val programs = Programs
    val fileSystem = FileSystem

    fun start() {
        log("Virtel Platform starting...", Log.INFO)
        val homePath = getHomePath()
        if(homePath == null) throw VirtelException()//TODO:Fix iOS and JVM
        else fileSystem.scan(homePath)
        if(fileSystem.isInstalled().not()){
            install()
        }
        //TODO:read configs
        Programs.scanPrograms()
        log("Virtel Launcher starting...", Log.INFO)
        Programs.startProgram("vladceresna.virtel.launcher")

        isLoading = false
        renderFunction()
        log("Virtel Platform started", Log.SUCCESS)
    }

    fun install(){
        fileSystem.install()
    }



    fun getCurrentRunnedProgram(): Program {
        return programs.currentRunnedProgram
    }

}