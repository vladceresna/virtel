package com.vladceresna.virtel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vladceresna.virtel.api.SystemApiImpl
import com.vladceresna.virtel.api.UiApiImpl
import com.vladceresna.virtel.screens.LoadingScreen
import com.vladceresna.virtel.screens.VirtelScreen
import com.vladceresna.virtel.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uniffi.vnative.getVirtelCenter

@Composable
fun App(){
    var isLoading by remember { mutableStateOf(true) }
    Box {
        LaunchedEffect(Unit) {
            delay(500)
            //after first recomposition only
            CoroutineScope(Job()).launch {
                val virtelCenter = getVirtelCenter()
                virtelCenter.initialize(UiApiImpl(), SystemApiImpl())
                virtelCenter.runApp("launcher.virtel.vladceresna")
                isLoading = false
            }
        }
        AppTheme(
            dynamicColor = true
        ) {
            if (isLoading) {
                LoadingScreen()
            } else {
                VirtelScreen()
            }
        }
    }
}