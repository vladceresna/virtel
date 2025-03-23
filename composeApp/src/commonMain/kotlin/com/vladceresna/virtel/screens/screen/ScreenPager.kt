package com.vladceresna.virtel.screens.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.terrakok.adaptivestack.HorizontalAdaptiveStack
import com.vladceresna.virtel.controllers.Flow
import com.vladceresna.virtel.controllers.Logger
import com.vladceresna.virtel.controllers.Program
import com.vladceresna.virtel.controllers.ProgramStatus
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.Step
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.screens.model.ScreenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenPager(
    screenModel: ScreenModel
) {
    val pagerState = rememberPagerState(pageCount = {
        screenModel.pageModels.size
    })

    Column (
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        var appsScreen = remember { VirtelSystem.appsScreen }


        if(!VirtelSystem.appsScreen.value) {
            HorizontalPager(
                modifier = Modifier.weight(1F).fillMaxWidth(),
                state = pagerState,
                contentPadding = PaddingValues(10.dp, 10.dp, 10.dp, 0.dp),
                pageSpacing = 8.dp,
                verticalAlignment = Alignment.Top,
            ) { page ->

                LaunchedEffect(pagerState.currentPage) {
                    screenModel.currentPageIndex.value = pagerState.currentPage
                }

                Page(
                    screenModel.pageModels[page], Modifier.fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .weight(1F), pagerState
                )
            }
        } else{
            Dashboard(
                modifier = Modifier.padding(10.dp,10.dp,10.dp,10.dp)
                    .weight(1F).fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.background)
                    .padding(PaddingValues(horizontal = 5.dp, vertical = 5.dp)),
                screenModel,
                pagerState
            )
        }

        /*Row(
            Modifier
                .fillMaxWidth()
                *//*.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Enter) {
                                VirtelSystem.appsScreen.value = !VirtelSystem.appsScreen.value
                            }
                        }
                    }
                }*//*
                .clickable {
                    VirtelSystem.appsScreen.value = !VirtelSystem.appsScreen.value
                }
            ,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                val width = if (pagerState.currentPage == iteration) 50.dp else 10.dp
                Box(
                    modifier = Modifier
                        .padding(2.dp,5.dp,2.dp,5.dp)
                        .clip(CircleShape)
                        .background(color)
                        .height(10.dp)
                        .width(width)

                )
            }
        }*/
    }
}
