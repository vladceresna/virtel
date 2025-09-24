package com.vladceresna.virtel

import android.content.Context
import android.os.Build
import java.io.File

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun getHomePath(): String? {
    //return "/storage/emulated/0/Android/data/com.vladceresna.virtel/files/"
    val file = VirtelContext.context.getExternalFilesDir(null)
    if (file != null) {
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath
    }
    throw Exception("Your device doesn't support Virtel")
}