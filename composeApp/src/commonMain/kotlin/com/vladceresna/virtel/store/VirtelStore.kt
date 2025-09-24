package com.vladceresna.virtel.store

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

object VirtelStore {
    var darkTheme = mutableStateOf(false)
    var seedColor = mutableStateOf(Color.Cyan)
}