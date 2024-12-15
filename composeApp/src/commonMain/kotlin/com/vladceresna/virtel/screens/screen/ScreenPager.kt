package com.vladceresna.virtel.screens.screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.Rgb
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.screens.model.ScreenModel


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenPager(
    screenModel: ScreenModel
) {
    val pagerState = rememberPagerState(pageCount = {
        screenModel.pageModels.size
    })

    Column (
        if (VirtelSystem.darkTheme.value) {
            Modifier.fillMaxSize().background(Color(0,20,5))
        } else {
            Modifier.fillMaxSize()
        }
    ) {
        var appsScreen = remember { VirtelSystem.appsScreen }


        if(!VirtelSystem.appsScreen.value) {
            HorizontalPager(
                modifier = Modifier.weight(1F).fillMaxWidth(),
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
                pageSpacing = 8.dp,
                verticalAlignment = Alignment.Top,
            ) { page ->

                LaunchedEffect(pagerState.currentPage) {
                    screenModel.currentPageIndex.value = pagerState.currentPage
                }

                Page(
                    screenModel.pageModels[page], Modifier.fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)).weight(1F), pagerState
                )
            }
        }
        if(VirtelSystem.appsScreen.value) {
            Column(
                modifier = Modifier.weight(1F).fillMaxWidth()
                    .padding(PaddingValues(horizontal = 20.dp, vertical = 20.dp))
                    .clip(RoundedCornerShape(20.dp)).background(getColorOfBackground())
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                screenModel.pageModels.get(screenModel.currentPageIndex.value)
                    .programViewModels.forEach {
                        ElevatedCard(
                            elevation = CardDefaults.elevatedCardElevation(2.dp)) {
                            Box(Modifier.padding(10.dp,10.dp)){
                                Text(it.program.appId)
                            }
                        }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Enter) {
                                VirtelSystem.appsScreen.value = !VirtelSystem.appsScreen.value
                            }
                        }
                    }
                }
            ,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                val width = if (pagerState.currentPage == iteration) 50.dp else 20.dp
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clip(CircleShape)
                        .background(color)
                        .height(20.dp)
                        .width(width)

                )
            }
        }
    }
}


fun getColorOfIcon():Color{
    return if(VirtelSystem.darkTheme.value){
        Color(255,255,255)
    } else {
        Color(0,0,0)
    }
}
fun getColorOfBackground():Color{
    return if(VirtelSystem.darkTheme.value){
        Color(0,10,0)
    } else {
        Color(200,255,230)
    }
}