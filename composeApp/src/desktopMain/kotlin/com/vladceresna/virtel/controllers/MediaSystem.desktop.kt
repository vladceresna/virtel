package com.vladceresna.virtel.controllers

import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.ByteArrayOutputStream
import java.io.File
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine
import javax.sound.sampled.TargetDataLine


data object DesktopMediaSystem {

    val format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000f, 16, 2, 4, 44100f, false)
    val info = DataLine.Info(TargetDataLine::class.java, format)
    lateinit var microphone: TargetDataLine
    lateinit var speakers: SourceDataLine

    fun start() {
        LibVosk.setLogLevel(LogLevel.DEBUG)

        val format = AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 60000f, 16, 2, 4, 44100f, false)
        val info = DataLine.Info(TargetDataLine::class.java, format)
        var microphone: TargetDataLine
        var speakers: SourceDataLine

        Model("src/main/java/com/vladceresna/lask/vosk-model-small-ru-0.22").use { model ->
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


                    while (bytesRead <= 100000000) {
                        var numBytesRead = microphone.read(b, 0, CHUNK_SIZE)
                        bytesRead += numBytesRead

                        out.write(b, 0, numBytesRead)
                        speakers.write(b, 0, numBytesRead)


                        if (recognizer.acceptWaveForm(b, numBytesRead)) {
                            val result = recognizer.result
                            print("$result ")
                            if (result.contains("ласк")) {
                                print(" SPEAKING - HELLO ")
                                MediaSystem.countHello++
                                Thread {
                                    try {
                                        AudioSystem.getAudioInputStream(File("src/main/java/com/vladceresna/lask/alarm.wav"))
                                            .use { audioInputStream ->
                                                var bytesReaded: Int
                                                val audioBuffer =
                                                    ByteArray(1024) // Adjust buffer size as needed
                                                while ((audioInputStream.read(
                                                        audioBuffer
                                                    )
                                                        .also {
                                                            bytesReaded = it
                                                        }) != -1
                                                ) {
                                                    speakers.write(audioBuffer, 0, bytesReaded)
                                                }
                                            }
                                    } catch (e: Exception) {
                                        print(e.message)
                                    }
                                }.start()
                            }
                        } else {
                            print(recognizer.partialResult + " ")
                        }




                        System.out.print(" ${MediaSystem.countHello} ")
                        val rms = calculateRMS(b, numBytesRead)
                        print("$rms ")
                        if (rms > MediaSystem.loudRMS) { // Loud
                            playWAV("src/main/java/com/vladceresna/lask/alarm.wav")
                        } else //normal



                            println()
                    }
                    println(recognizer.finalResult)
                    speakers.drain()
                    speakers.close()
                    microphone.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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
            print(e.message)
        }
    }
}

actual fun runSTT() {
    DesktopMediaSystem.start()
}