package com.vladceresna.virtel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.compose.AppTheme
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.screens.LoadingScreen
import com.vladceresna.virtel.screens.VirtelScreen
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(theme: Boolean = isSystemInDarkTheme()) {
    var darkTheme by remember { mutableStateOf(theme) }
    var rd by remember { mutableStateOf(true) }
    val virtelSystem = VirtelSystem
    virtelSystem.renderFunction = { rd = !rd }
    LaunchedEffect(Unit){
        //after first recomposition only
        CoroutineScope(Job()).launch {
            virtelSystem.start()
            rd = !rd
        }

    }
    LaunchedEffect(rd) {
        //after recomposition
        CoroutineScope(Job()).launch {
            rd = !rd
            log("RenderFunc $rd", Log.DEBUG)
        }
    }
    AppTheme(rd){
        if (virtelSystem.isLoading) {
            println("rrend")
            LoadingScreen(rd)
        } else {
            println("rrend")
            VirtelScreen(rd)
        }
    }
}