package com.vladceresna.virtel


import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        icon = BitmapPainter(useResource(
            "logo.png", ::loadImageBitmap)),
        onCloseRequest = ::exitApplication,
        title = "Virtel",
        state = rememberWindowState(
            placement = WindowPlacement.Fullscreen,
            position = WindowPosition(Alignment.Center)
        ),
        alwaysOnTop = true,
    ) {
        App()
    }
}