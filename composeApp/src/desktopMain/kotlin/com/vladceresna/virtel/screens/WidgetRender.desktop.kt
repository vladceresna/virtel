package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vladceresna.virtel.screens.model.WidgetModel

@Composable
actual fun Adaptive(
    leftModel: WidgetModel,
    rightModel: WidgetModel
) {
    Row {
        SizedWidget(leftModel,
            if(leftModel.weight.value != 0f) {
                Modifier.weight(leftModel.weight.value)
            } else Modifier
        )
        SizedWidget(rightModel,
            if(rightModel.weight.value != 0f) {
                Modifier.weight(rightModel.weight.value)
            } else Modifier
        )
    }
}
