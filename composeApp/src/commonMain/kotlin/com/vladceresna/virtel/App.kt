package com.vladceresna.virtel

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
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
            delay(200)
            //after first recomposition only
            val virtelCenter = getVirtelCenter()
            CoroutineScope(Job()).launch {
                delay(100)
                isLoading = false
            }
            Thread {
                virtelCenter.initialize(UiApiImpl(), SystemApiImpl())
            }.start()
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