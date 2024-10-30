package com.vladceresna.virtel

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun getHomePath(): String? {
    return "/storage/emulated/0/Android/data/com.vladceresna.virtel/files/"
}