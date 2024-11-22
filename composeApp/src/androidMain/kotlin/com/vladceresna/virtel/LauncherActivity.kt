package com.vladceresna.virtel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.vladceresna.virtel.controllers.VirtelSystem

class LauncherActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VirtelSystem.applicationContext = applicationContext
        setContent {
            VirtelApp()
        }

    }
}
