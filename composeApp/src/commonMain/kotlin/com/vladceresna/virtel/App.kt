package com.vladceresna.virtel

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.compose.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(theme: Boolean = isSystemInDarkTheme()) {
    var loading by remember { mutableStateOf(true) }
    var darkTheme by remember { mutableStateOf(theme) }
    if (loading){
        Column (
            Modifier.fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterHorizontally
        ) {
            Text("Virtel")
        }
    } else {
        AppTheme {
            Screen("vladceresna.virtel.launcher", darkTheme)
        }
    }

}
