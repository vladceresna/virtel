package com.vladceresna.virtel.controllers

import com.vladceresna.virtel.getHomePath
import com.vladceresna.virtel.ai.toSpeech
import com.vladceresna.virtel.other.VirtelException
import com.vladceresna.virtel.screens.model.ScreenModel
import com.vladceresna.virtel.screens.model.VirtelScreenViewModel
import okio.Path.Companion.toPath
import okio.SYSTEM

data object VirtelSystem {

    var screenModel:ScreenModel = ScreenModel()


    var isLoading: Boolean = true

    val programs = Programs
    val fileSystem = FileSystem

    var labs = ""
    var pico = ""
    var llm = ""
    var vosk = ""
    var native = ""//android stt/tts

    var echo = false


    lateinit var applicationContext:Any

    lateinit var recognizeMicrophone:() -> Unit


    var isSaying = false

    fun start() {


        log("Virtel Platform starting...", Log.INFO)
        val homePath = getHomePath()
        if(homePath == null) throw VirtelException("Home path not found, " +
                "maybe your device is not supported by Virtel") //TODO:Fix iOS
        else fileSystem.scan(homePath)
        if(fileSystem.isInstalled().not()){
            install()
        }
        readConfig()

        try {
            val text = "Привіт, ярди в кедрах! Я тут щоб автоматизувати твою рутину."
            var ttsFile = FileSystem.userFilesPath + "/virtel/tts-cache/$text.mp3"
            if (!okio.FileSystem.SYSTEM.exists(ttsFile.toPath())) {
                toSpeech(text, labs, ttsFile)
            }
            playMP3(ttsFile)
        } catch (e: Exception) {}

        Programs.scanPrograms()
        log("Virtel Launcher starting...", Log.INFO)
        Programs.startProgram("vladceresna.virtel.launcher")

        isLoading = false
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
                var pair = it.split( "=", limit = 2)
                when(pair.get(0).trim()){
                    "labs" -> {labs = pair.get(1).trim()}
                    "pico" -> {pico = pair.get(1).trim()}
                    "llm" -> {llm = pair.get(1).trim().replace("$",FileSystem.systemPath)}
                    "vosk" -> {vosk = pair.get(1).trim().replace("$",FileSystem.systemPath)}
                    "native" -> {native = pair.get(1).trim()}
                    else -> {}
                }
            }
        } else {
            okio.FileSystem.SYSTEM.write(path){writeUtf8("")}
        }
    }

}