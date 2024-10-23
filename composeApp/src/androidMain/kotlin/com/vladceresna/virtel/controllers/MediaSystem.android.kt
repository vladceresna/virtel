package com.vladceresna.virtel.controllers

import android.media.AudioFormat
import androidx.activity.ComponentActivity
import com.vladceresna.virtel.MainActivity
import com.vladceresna.virtel.controllers.MediaSystem.countScanned
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.ByteArrayOutputStream
import java.io.File


actual fun runSTT() {
    (VirtelSystem.applicationContext as MainActivity).recognizeMicrophone()
}
