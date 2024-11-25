package com.vladceresna.virtel.screens.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.screens.model.ScreenModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenPager(
    screenModel: ScreenModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = {
        screenModel.pageModels.size
    })

    Column (
        modifier = modifier
    ) {
        HorizontalPager(
            modifier = Modifier.weight(1F),
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
            pageSpacing = 8.dp,
            verticalAlignment = Alignment.Top,
        ) { page ->

            LaunchedEffect(pagerState.currentPage){
                screenModel.currentPageIndex.value = pagerState.currentPage
            }

            Page(screenModel.pageModels[page],Modifier.fillMaxSize()
                .clip(RoundedCornerShape(20.dp)).weight(1F))
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}