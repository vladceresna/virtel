package com.vladceresna.virtel.controllers

import android.util.Log

actual fun logAny(message: String) {
    Log.i("VIRTEL", message)
}