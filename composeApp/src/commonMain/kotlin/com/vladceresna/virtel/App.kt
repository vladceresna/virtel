package com.vladceresna.virtel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.compose.AppTheme
import com.vladceresna.virtel.other.VirtelException
import com.vladceresna.virtel.screens.LoadingScreen
import com.vladceresna.virtel.screens.VirtelScreen
import com.vladceresna.virtel.runner.VirtelSystem
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
        virtelSystem.start()
    }
    LaunchedEffect(rd) {
        //after recomposition

    }
    AppTheme(rd){
        if (virtelSystem.isLoading) {
            LoadingScreen()
        } else {
            VirtelScreen()
        }
    }
}