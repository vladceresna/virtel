package com.vladceresna.virtel.controllers

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.modifier.modifierLocalMapOf
import com.vladceresna.virtel.getHomePath
import com.vladceresna.virtel.ai.toSpeech
import com.vladceresna.virtel.other.VirtelException
import com.vladceresna.virtel.screens.model.ScreenModel
import com.vladceresna.virtel.screens.model.VirtelScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okio.Path.Companion.toPath
import okio.SYSTEM
import uniffi.vnative.*

data object VirtelSystem {

    var screenModel:ScreenModel = ScreenModel()

    var darkTheme = mutableStateOf(true)
    var appsScreen = mutableStateOf(false)


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


    var isSawHello = false



    fun start() {

        log("Virtel Platform starting...", Log.INFO)

        val homePath = getHomePath()
        if(homePath == null) throw VirtelException("Home path not found, " +
                "maybe your device is not supported by Virtel") //TODO:Fix iOS
        else FileSystem.scan(homePath)
        if(FileSystem.isInstalled().not()){
            install()
        }
        readConfig()

        Programs.scanPrograms()
        log("Virtel Launcher starting...", Log.INFO)

        CoroutineScope(Job()).launch {

            isSawHello = true
            try {
                var text = "Вітаю!"
                var ttsFile = FileSystem.userFilesPath + "/virtel/tts-cache/$text.mp3"
                if (!okio.FileSystem.SYSTEM.exists(ttsFile.toPath())) {
                    //toSpeech(text, labs, ttsFile)
                    ttsSayLang(text, ttsFile, "uk")
                } else playMp3(ttsFile)
            } catch (e: Exception) {
            }
        }
        CoroutineScope(Job()).launch {
            Programs.startProgram("vladceresna.virtel.launcher")
        }
        log("Virtel Platform started", Log.SUCCESS)
    }

    fun install(){
        fileSystem.install()
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