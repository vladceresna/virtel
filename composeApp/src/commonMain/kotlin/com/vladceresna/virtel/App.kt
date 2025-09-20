package com.vladceresna.virtel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vladceresna.virtel.screens.LoadingScreen
import com.vladceresna.virtel.screens.VirtelScreen
import com.vladceresna.virtel.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun App(){
    var isLoading by remember { mutableStateOf(true) }
    Box {
        LaunchedEffect(Unit) {
            //after first recomposition only
            CoroutineScope(Job()).launch {
                delay(2000)
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