package com.vladceresna.virtel.screens.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.screens.model.PageModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Page(
    pageModel: PageModel,
    modifier: Modifier, pagerState: PagerState
) {
    Column (
        Modifier.fillMaxSize()
    ){
        Box (modifier = modifier){
            if(pageModel.programViewModels.size>0) {
                ProgramLayer(pageModel.programViewModels.last(), Modifier.fillMaxSize())
            } else {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
                ) {
                    IconButton(
                        onClick = {
                                Programs.startProgram("vladceresna.virtel.launcher")
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                .size(200.dp)
                                .clip(RoundedCornerShape(70.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                            Icon(
                                imageVector = Icons.Outlined.AddCircle,
                                contentDescription = "Plus",
                                modifier = Modifier.size(150.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                    }

                    IconButton(
                            onClick = {
                                if (VirtelSystem.screenModel.pageModels.size > 1) {
                                    VirtelSystem.screenModel.pageModels
                                        .removeAt(VirtelSystem.screenModel.currentPageIndex.value)
                                }
                            },
                            modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)
                                .size(200.dp)
                                .clip(RoundedCornerShape(70.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                modifier = Modifier.size(150.dp),
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                }
            }
        }
        Row(
            modifier = Modifier.padding(0.dp,10.dp,0.dp,10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row (
                Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface),
                horizontalArrangement = Arrangement.Start
            ){
                IconButton(onClick = {
                    if (pagerState.currentPage != 0) {
                        runBlocking {
                            pagerState.scrollToPage(pagerState.currentPage - 1)
                        }
                    }
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Localized description",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = {
                        if (pagerState.currentPage < pagerState.pageCount - 1) {
                            runBlocking {
                                pagerState.scrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Localized description",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            Row (
                Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        VirtelSystem.appsScreen.value = !VirtelSystem.appsScreen.value
                    }
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                    repeat(pagerState.pageCount) { iteration ->
                        val color =
                            if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                        val width = if (pagerState.currentPage == iteration) 50.dp else 10.dp
                        Box(
                            modifier = Modifier
                                .padding(2.dp, 5.dp, 2.dp, 5.dp)
                                .clip(CircleShape)
                                .background(color)
                                .height(10.dp)
                                .width(width)

                        )
                    }
            }




            Box(Modifier.weight(1f)){}
            Row (
                Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ){
                Switch(
                    VirtelSystem.darkTheme.value,
                    onCheckedChange = {
                        VirtelSystem.darkTheme.value = it
                    }
                )
            }

            /*IconButton(onClick = {
                try {
                    pageModel.backClick.value()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Modifier.weight(1f)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }*/

        }
    }
}