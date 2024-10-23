package com.vladceresna.virtel.controllers

import android.media.MediaPlayer


actual fun playMP3(filePath: String) {
    playWAV(filePath)
}

actual fun playWAV(filePath: String) {
    var mp = MediaPlayer()

    try {
        log("Playing $filePath", Log.INFO)
        mp.setDataSource(filePath)
        mp.prepare()
        mp.setOnCompletionListener {
            mp.release()
        }
        mp.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}