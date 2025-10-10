package com.vladceresna.virtel.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.store.UiStore
import com.vladceresna.virtel.store.Widget
import com.vladceresna.virtel.store.Window


@Composable
fun VirtelScreen(

) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Shipmilk(UiStore.windows)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Shipmilk(windows: Map<String, Window>){

    Column (
        modifier = Modifier.fillMaxSize()
    ) {
        for ((key, window) in windows){
            Box(
                Modifier
                    .clip(RoundedCornerShape(20.dp)),
            ) {
                window.render(key)
            }
        }
    }
}
