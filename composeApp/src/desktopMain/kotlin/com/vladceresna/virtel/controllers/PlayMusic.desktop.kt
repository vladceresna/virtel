package com.vladceresna.virtel.controllers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileInputStream


actual fun playMP3(filePath: String) {
//    CoroutineScope(Job()).launch {
//        val stream: FileInputStream = FileInputStream(filePath)
//        val player = Player(stream)
//        player.play()
//    }
    playWAV(filePath)
}

actual fun playWAV(filePath: String) {
    CoroutineScope(Job()).launch {
        DesktopMediaSystem.also {
            it.start()
            it.playWAV(filePath)
        }
    }
}