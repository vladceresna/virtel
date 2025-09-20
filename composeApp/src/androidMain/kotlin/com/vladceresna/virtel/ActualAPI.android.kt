package com.vladceresna.virtel

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.provider.Settings
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import java.io.File


actual fun isFileExists(path: String): Boolean = File(path).exists()
actual fun getHttpClient(): HttpClient {
    return HttpClient(OkHttp)
}

actual fun openSettings() {
    val intent = Intent(Settings.ACTION_SETTINGS)
    intent.flags = FLAG_ACTIVITY_NEW_TASK
    startActivity(VirtelContext.context!!, intent, null)
}
actual fun openApp(packageName: String) {
    try {
        val intent = VirtelContext.context!!.packageManager.getLaunchIntentForPackage(packageName)
        intent!!.flags = FLAG_ACTIVITY_NEW_TASK
        startActivity(VirtelContext.context!!, intent, null)
    } catch (e: Exception) { }
}