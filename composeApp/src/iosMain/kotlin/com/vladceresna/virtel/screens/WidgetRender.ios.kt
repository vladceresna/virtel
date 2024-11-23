package com.vladceresna.virtel.screens

import androidx.compose.runtime.Composable
import com.vladceresna.virtel.screens.model.WidgetModel


@Composable
actual fun Adaptive(
    left: @Composable () -> Unit,
    right: @Composable () -> Unit,
    leftModel: WidgetModel,
    rightModel: WidgetModel
) {
}