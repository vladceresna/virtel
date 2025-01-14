package com.vladceresna.virtel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.compose.AppTheme
import com.vladceresna.virtel.screens.LoadingScreen
import com.vladceresna.virtel.screens.VirtelScreen
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.screens.model.VirtelScreenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun App(screenModel: VirtelScreenViewModel = viewModel<VirtelScreenViewModel>()){
    Box(
        Modifier.onKeyEvent {
            when (it.key) {
                androidx.compose.ui.input.key.Key.DirectionUp -> {
                    VirtelSystem.appsScreen.value = true
                    true
                }
                androidx.compose.ui.input.key.Key.DirectionDown -> {
                    VirtelSystem.appsScreen.value = false
                    true
                }
                else -> false
            }
        }
    ) {
        LaunchedEffect(Unit) {
            //after first recomposition only
            CoroutineScope(Job()).launch {

                VirtelSystem.screenModel = screenModel.screenModel
                VirtelSystem.start()
                screenModel.isLoading = false
            }
        }
        AppTheme {
            if (screenModel.isLoading) {
                LoadingScreen()
            } else {
                VirtelScreen(screenModel)
            }
        }
    }
}