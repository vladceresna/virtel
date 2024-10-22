package com.vladceresna.virtel.controllers

import javazoom.jl.player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileInputStream

actual fun playMP3(filePath: String) {
    CoroutineScope(Job()).launch {
        val stream: FileInputStream = FileInputStream(filePath)
        val player = Player(stream)
        player.play()
    }
}

actual fun playWAV(filePath: String) {

}