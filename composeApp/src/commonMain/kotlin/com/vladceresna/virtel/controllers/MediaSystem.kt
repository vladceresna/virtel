package com.vladceresna.virtel.controllers

import kotlin.math.sqrt


object MediaSystem {
    var countScanned: Int = 0
    var numBytesRead: Int = 0

    var loudRMS = 18000
    var rmsEnabled = true
    var loudLevel = 20000

    var allResult = ""
    var lastResult = ""

    var wakes: MutableMap<String, Pair<String, String>> = mutableMapOf() //word:(file:appId)

    var isWorks = true

}

expect fun runSTT()



fun calculateRMS(audioData: ByteArray, numBytes: Int): Double {
    // set audiodata value as audiodata last 100 bytes
    var audioData = audioData
    if (audioData.size > MediaSystem.loudLevel) {
        audioData = audioData.copyOfRange(audioData.size - 10000, audioData.size)
    }

    try {
        // Calculate mean squared
        var sum = 0.0
        for (i in 0 until numBytes) {
            // Convert byte to signed short (assuming 16-bit PCM)
            val sample =
                ((audioData[i].toInt() and 0xFF) shl 8 or (audioData[i + 1].toInt() and 0xFF)).toShort()
            sum += (sample * sample).toDouble()
        }
        val meanSquare = sum / numBytes
        return sqrt(meanSquare)
    } catch (e: Exception) {
        return 5000.0
    }
}