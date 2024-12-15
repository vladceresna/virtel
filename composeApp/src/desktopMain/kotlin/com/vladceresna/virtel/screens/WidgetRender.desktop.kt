package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vladceresna.virtel.screens.model.WidgetModel

@Composable
actual fun Adaptive(
    modifier: Modifier,
    leftModel: WidgetModel,
    rightModel: WidgetModel
) {
    Row(modifier) {
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
