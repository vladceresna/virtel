package com.vladceresna.virtel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
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
    LaunchedEffect(Unit){
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