package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.vladceresna.virtel.screens.model.WidgetModel

@Composable
actual fun Adaptive(
    leftModel: WidgetModel,
    rightModel: WidgetModel
) {
    Column {
        left()
        right()
    }
}