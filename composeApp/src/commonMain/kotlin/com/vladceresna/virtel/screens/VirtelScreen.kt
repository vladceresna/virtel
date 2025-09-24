package com.vladceresna.virtel.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.vladceresna.virtel.store.UiStore
import com.vladceresna.virtel.store.Widget


@Composable
fun VirtelScreen(

) {
    Text("Apps")
    UiStore.windows.forEach { window ->
        window.widgetsTree.forEach { widget ->
            widget.render()
        }
    }
}
