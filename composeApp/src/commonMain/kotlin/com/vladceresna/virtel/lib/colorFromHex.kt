package com.vladceresna.virtel.lib

import androidx.compose.ui.graphics.Color

fun colorFromHex(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    val colorLong = cleanHex.toLong(16)
    val argb = if (cleanHex.length == 6) {
        0xFF000000 or colorLong
    } else {
        colorLong
    }
    return Color(argb.toInt())
}