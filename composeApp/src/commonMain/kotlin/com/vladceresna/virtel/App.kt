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
        virtelSystem.renderFunction()
        //after first recomposition only
        CoroutineScope(Job()).launch {
            virtelSystem.start()
            virtelSystem.renderFunction()
        }
    }
    LaunchedEffect(rd) {
        virtelSystem.renderFunction()
        //after recomposition
    }
    AppTheme(rd){
        rd
        if (virtelSystem.isLoading) {
            LoadingScreen(rd)
        } else {
            VirtelScreen(rd)
        }
    }
}