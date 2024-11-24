package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vladceresna.virtel.screens.model.WidgetModel

@Composable
actual fun Adaptive(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
    leftModel: WidgetModel,
    rightModel: WidgetModel
) {
    Row {
        if (leftModel.weight.value == -1F) {
            Box(Modifier)
            { left() }
        } else {
            Box(Modifier.weight(leftModel.weight.value))
            { left() }
        }
        if (rightModel.weight.value == -1F) {
            Box(Modifier)
            { right() }
        } else {
            Box(Modifier.weight(rightModel.weight.value))
            { right() }
        }
    }
}