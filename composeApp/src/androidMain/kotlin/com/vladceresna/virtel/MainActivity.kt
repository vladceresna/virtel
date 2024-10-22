package com.vladceresna.virtel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.vosk.android.RecognitionListener
import java.lang.Exception

class MainActivity : ComponentActivity(), RecognitionListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }

    override fun onPartialResult(hypothesis: String?) {
        TODO("Not yet implemented")
    }

    override fun onResult(hypothesis: String?) {
        TODO("Not yet implemented")
    }

    override fun onFinalResult(hypothesis: String?) {
        TODO("Not yet implemented")
    }

    override fun onError(exception: Exception?) {
        TODO("Not yet implemented")
    }

    override fun onTimeout() {
        TODO("Not yet implemented")
    }
}


@Preview
@Composable
fun AppAndroidPreview() {
    App()
}