package com.vladceresna.virtel.controllers

import ai.picovoice.picovoice.Picovoice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.DataLine.Info
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine


data object DesktopMediaSystem {

    lateinit var format: AudioFormat
    lateinit var info: Info
    lateinit var microphone: TargetDataLine
    lateinit var speakers: SourceDataLine

    fun start() {

        try {

            MediaSystem.allResult = ""
            MediaSystem.lastResult = ""

            var format =
                AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000f, 16, 2, 4, 44100f, false)
            var info = Info(TargetDataLine::class.java, format)
            var microphone: TargetDataLine
            var speakers: SourceDataLine

            println("Starting model")

            try {
                var path = VirtelSystem.vosk
                println(path)
                println("3")
                try {

                    microphone = AudioSystem.getLine(info) as TargetDataLine
                    microphone.open(format)
                    microphone.start()
                    val out = ByteArrayOutputStream()

                    val CHUNK_SIZE = 1024
                    var bytesRead = 0

                    val dataLineInfo =
                        DataLine.Info(SourceDataLine::class.java, format)
                    speakers = AudioSystem.getLine(dataLineInfo) as SourceDataLine
                    speakers.open(format)
                    speakers.start()
                    val b = ByteArray(4096)

                    while (bytesRead <= 100000000) {
                        if (MediaSystem.isWorks) {
                            var numBytesRead = microphone.read(b, 0, CHUNK_SIZE)
                            bytesRead += numBytesRead

                            out.write(b, 0, numBytesRead)
                            if (VirtelSystem.echo) speakers.write(b, 0, numBytesRead)

                            if (true) {
                                MediaSystem.countScanned++
                                val result = ""
                                log("Currently recognised result: $result", Log.INFO)
                                MediaSystem.allResult += result
                                MediaSystem.lastResult += result
                                MediaSystem.wakes.forEach {
                                    if (result.contains(it.key)) {
                                        MediaSystem.lastResult = ""
                                        val file = it.value.first
                                        val appId = it.value.second
                                        CoroutineScope(Job()).launch {
                                            Programs.findProgram(appId)
                                                .runFlow(file, file + "-flow")
                                        }
                                    }
                                }
                            } else {
                                log(
                                    "Partially recognised currently: " + "",
                                    Log.DEBUG
                                )
                            }

                            if (MediaSystem.rmsEnabled) {
                                val rms = calculateRMS(b, numBytesRead)
                                if (rms > MediaSystem.loudRMS) {
                                    log("RMS level (Loud): $rms", Log.DEBUG)
                                    playWAV(FileSystem.userFilesPath + "/virtel/ringtones/alarm.wav")
                                } else {
                                    log("RMS level (Normal): $rms", Log.DEBUG)
                                }
                            }
                        } else {
                            break
                        }
                    }
                    speakers.drain()
                    speakers.close()
                    microphone.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } catch (e:Exception) {
                e.printStackTrace();
            }
        } catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun playWAV(path:String){
        try {
            AudioSystem.getAudioInputStream(File(path))
                .use { audioInputStream ->
                    var bytesReaded: Int
                    val audioBuffer =
                        ByteArray(1024) // Adjust buffer size as needed
                    while ((audioInputStream.read(audioBuffer)
                            .also { bytesReaded = it }) != -1
                    ) {
                        speakers.write(audioBuffer, 0, bytesReaded)
                    }
                }
        } catch (e: Exception) {
            log(e.message.toString(), Log.ERROR)
        }
    }
}

actual fun runSTT() {
    DesktopMediaSystem.start()
}