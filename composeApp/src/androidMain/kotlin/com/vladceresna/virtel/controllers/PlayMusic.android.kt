package com.vladceresna.virtel.controllers

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File


actual fun playMP3(filePath: String) {
    playWAV(filePath)
}

actual fun playWAV(filePath: String) {
    runBlocking {
        log("Playing $filePath", Log.INFO)
        val myUri: Uri = Uri.fromFile(File(filePath)) // initialize Uri here


        while(VirtelSystem.isSaying){}
        VirtelSystem.isSaying = true
        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(VirtelSystem.applicationContext as Context, myUri)
            prepare()
            start()
        }
        while(mediaPlayer.isPlaying){}
        VirtelSystem.isSaying = false

    }
}