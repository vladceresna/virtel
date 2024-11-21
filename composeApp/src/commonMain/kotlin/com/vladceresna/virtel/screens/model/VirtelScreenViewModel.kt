package com.vladceresna.virtel.screens.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class VirtelScreenViewModel:ViewModel() {
    var screenModel:ScreenModel by mutableStateOf(ScreenModel())
}