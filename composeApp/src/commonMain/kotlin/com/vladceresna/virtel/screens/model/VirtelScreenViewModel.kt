package com.vladceresna.virtel.screens.model

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class VirtelScreenViewModel:ViewModel() {
    var root = mutableStateOf(WidgetModel())
    var bottom = mutableStateOf(WidgetModel())
    var top = mutableStateOf(WidgetModel())

    var settingsClick:() -> Unit = {

    }
    var homeClick:() -> Unit = {

    }
    var backClick:() -> Unit = {

    }
}