package com.vladceresna.virtel.controllers

import android.media.AudioFormat
import com.vladceresna.virtel.controllers.MediaSystem.countScanned
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import java.io.ByteArrayOutputStream
import java.io.File


actual fun runSTT() {
    LibVosk.setLogLevel(LogLevel.DEBUG)

    val format = AudioFormat(AudioFormat.ENCODING_PCM_16BIT, 60000, 16, 2, 4, 44100, false)
    val info: DataLine.Info = Info(TargetDataLine::class.java, format)
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

                val dataLineInfo: DataLine.Info = Info(SourceDataLine::class.java, format)
                speakers = AudioSystem.getLine(dataLineInfo) as SourceDataLine
                speakers.open(format)
                speakers.start()
                val b = ByteArray(4096)


                while (bytesRead <= 100000000) {
                    numBytesRead = microphone.read(b, 0, CHUNK_SIZE)
                    bytesRead += numBytesRead

                    out.write(b, 0, numBytesRead)
                    speakers.write(b, 0, numBytesRead)


                    if (recognizer.acceptWaveForm(b, numBytesRead)) {
                        val result = recognizer.result
                        print("$result ")
                        if (result.contains("ласк")) {
                            print(" SPEAKING - HELLO ")
                            countScanned++
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




                    System.out.print(" $countScanned ")
                    val rms = calculateRMS(b, numBytesRead)
                    print("$rms ")
                    if (rms > 18000) {
                        print(" SPEAKING - TOO LOUD ")
                        try {
                            AudioSystem.getAudioInputStream(File("src/main/java/com/vladceresna/lask/alarm.wav"))
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
                    } else print(" SPEAKING - NORMAL ")



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