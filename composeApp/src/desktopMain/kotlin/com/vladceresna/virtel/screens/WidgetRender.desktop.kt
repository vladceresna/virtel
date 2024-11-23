package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
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
    Row () {
        if (leftModel.weight == -1F) {
            Box(Modifier.wrapContentWidth())
            { left() }
        } else {
            Box(Modifier.weight(leftModel.weight))
            { left() }
        }
        if (rightModel.weight == -1F) {
            Box(Modifier.wrapContentWidth())
            { right() }
        } else {
            Box(Modifier.weight(rightModel.weight))
            { right() }
        }
    }
}