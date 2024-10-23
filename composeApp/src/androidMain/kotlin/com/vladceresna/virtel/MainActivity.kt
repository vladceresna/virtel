package com.vladceresna.virtel

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.MediaSystem
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import org.vosk.android.SpeechStreamService
import org.vosk.android.StorageService
import java.io.IOException


class MainActivity : ComponentActivity(), RecognitionListener {

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var speechStreamService: SpeechStreamService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)






        VirtelSystem.applicationContext = getApplicationContext()
        LibVosk.setLogLevel(LogLevel.DEBUG)

        setContent {
            App()
        }

        while(VirtelSystem.vosk.equals("")) {}
        initModel()

    }

    fun initModel() {
        try {
            StorageService.unpack(
                this, VirtelSystem.vosk, "model",
                { model: Model ->
                    this.model = model
                },
                { exception: IOException -> })
        } catch (e: Exception) { }
    }

    override fun onPartialResult(hypothesis: String?) {
        //MediaSystem.allResult += hypothesis + "\n"
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (speechService != null) {
            speechService!!.stop()
            speechService!!.shutdown()
        }
        speechStreamService?.stop()
    }

    override fun onResult(hypothesis: String) {
        val result = hypothesis + "\n"
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
    }

    override fun onFinalResult(hypothesis: String?) {
        //MediaSystem.allResult += hypothesis + "\n"
        if (speechStreamService != null) {
            speechStreamService = null
        }
    }

    override fun onError(exception: Exception?) {}
    override fun onTimeout() {}
    fun recognizeMicrophone() {
        if (speechService != null) {
            speechService!!.stop()
            speechService = null
        } else {
            try {
                val rec = Recognizer(model, 16000.0f)
                speechService = SpeechService(rec, 16000.0f)
                speechService!!.startListening(this)
            } catch (e: IOException) {
            }
        }
        CoroutineScope(Job()).launch {
            while (true){
                if (!MediaSystem.isWorks){
                    pause(true)
                }
            }
        }
    }

    fun pause(checked: Boolean) {
        if (speechService != null) {
            speechService!!.setPause(checked)
        }
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}