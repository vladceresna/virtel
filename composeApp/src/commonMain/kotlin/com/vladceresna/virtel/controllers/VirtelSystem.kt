package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.getHomePath
import com.vladceresna.virtel.openai.toSpeech
import com.vladceresna.virtel.other.VirtelException
import okio.Path.Companion.toPath
import okio.SYSTEM

data object VirtelSystem {
    var isLoading: Boolean = true
    lateinit var renderFunction: () -> Unit

    val programs = Programs
    val fileSystem = FileSystem

    var labs = ""


    fun start() {


        log("Virtel Platform starting...", Log.INFO)
        val homePath = getHomePath()
        if(homePath == null) throw VirtelException()//TODO:Fix iOS and JVM
        else fileSystem.scan(homePath)
        if(fileSystem.isInstalled().not()){
            install()
        }
        readConfig()

        val text = "Привет, ярды в кедрах! Я тут чтобы автоматизировать твою рутину!"
        var ttsFile = FileSystem.userFilesPath+"/virtel/$text.mp3"
        if (!okio.FileSystem.SYSTEM.exists(ttsFile.toPath())) {
            toSpeech(text, labs, ttsFile)
        }
        playMP3(ttsFile)

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

    fun readConfig(){
        val path = FileSystem.systemConfigPath.toPath()
        if(okio.FileSystem.SYSTEM.exists(path)){
            val content = okio.FileSystem.SYSTEM.read(path){readUtf8()}
            content.split(";").forEach {
                var pair = it.split("=")
                when(pair.get(0).trim()){
                    "labs" -> {labs = pair.get(1).trim()}
                    else -> {}
                }
            }
        } else {
            okio.FileSystem.SYSTEM.write(path){writeUtf8("")}
        }
    }

}