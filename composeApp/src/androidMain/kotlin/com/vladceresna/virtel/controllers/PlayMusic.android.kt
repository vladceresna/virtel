package com.vladceresna.virtel.controllers

import android.R.attr.path
import android.content.Context
import android.media.MediaPlayer
import android.os.PowerManager
import javazoom.jl.player.Player
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream


actual fun playMP3(filePath: String) {
    CoroutineScope(Job()).launch {
        val stream = FileInputStream(filePath)
        val player = Player(stream)
        player.play()
    }
}

actual fun playWAV(filePath: String) {
    val mp = MediaPlayer()

    try {
        mp.setDataSource(filePath)
        mp.setWakeMode(VirtelSystem.applicationContext as Context, PowerManager.PARTIAL_WAKE_LOCK)
        mp.prepare()
        mp.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}