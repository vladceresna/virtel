package com.vladceresna.virtel.lib

import androidx.compose.runtime.*
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.Composable

@Composable
fun rememberCodeEditorState() = remember {
    CodeEditorState()
}

class CodeEditorState {
    val editorText = mutableStateOf("content")
    var showSettings by mutableStateOf(true)

    val run = {
        println("run: ${editorText.value}")
    }

    val close = {

    }
}