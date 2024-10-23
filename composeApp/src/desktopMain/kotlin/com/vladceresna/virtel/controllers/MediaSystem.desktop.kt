package com.vladceresna.virtel.controllers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
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
            LibVosk.setLogLevel(LogLevel.INFO)

            MediaSystem.allResult = ""
            MediaSystem.lastResult = ""

            format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000f, 16, 2, 4, 44100f, false)
            info = DataLine.Info(TargetDataLine::class.java, format)
            microphone = AudioSystem.getLine(info) as TargetDataLine
            val dataLineInfo =
                DataLine.Info(SourceDataLine::class.java, format)
            speakers = AudioSystem.getLine(dataLineInfo) as SourceDataLine

            Model(FileSystem.userFilesPath + VirtelSystem.vosk).use { model ->
                Recognizer(model, 120000f).use { recognizer ->
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

                        while (true) {
                            if (MediaSystem.isWorks) {
                                var numBytesRead = microphone.read(b, 0, CHUNK_SIZE)
                                bytesRead += numBytesRead

                                out.write(b, 0, numBytesRead)
                                if (VirtelSystem.echo) speakers.write(b, 0, numBytesRead)

                                if (recognizer.acceptWaveForm(b, numBytesRead)) {
                                    MediaSystem.countScanned++
                                    val result = recognizer.result
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
                                        "Partially recognised currently: " + recognizer.partialResult,
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
                }
            }
        } catch (e:Throwable){
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