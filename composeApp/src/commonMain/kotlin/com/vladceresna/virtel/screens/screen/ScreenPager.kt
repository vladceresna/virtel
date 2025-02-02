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
        if (VirtelSystem.darkTheme.value) {
            Modifier.background(Color(0,20,5)).fillMaxSize()
        } else {
            Modifier.fillMaxSize()
        }
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
                        .clip(RoundedCornerShape(20.dp)).weight(1F), pagerState
                )
            }
        } else{
            Column(
                modifier = Modifier.padding(10.dp,10.dp,10.dp,0.dp)
                    .weight(1F).fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.background)
                    .padding(PaddingValues(horizontal = 5.dp, vertical = 5.dp))
                ,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                HorizontalAdaptiveStack {
                    Column(
                        Modifier
                            .padding(5.dp,5.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .fillMaxSize()
                    ) {

                        var scrollState = rememberScrollState(0)
                        Column(
                            Modifier.verticalScroll(scrollState)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(10.dp).weight(1f).animateContentSize().fillMaxWidth()
                        ) {
                            Text("Running programs",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Black, fontSize = 25.sp, modifier = Modifier.padding(10.dp))
                            screenModel.pageModels.get(screenModel.currentPageIndex.value)
                                .programViewModels.forEachIndexed { index, it ->
                                    Column (Modifier.padding(10.dp,5.dp).fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .clickable {
                                            VirtelSystem.appsScreen.value = false
                                            screenModel.pageModels.get(screenModel.currentPageIndex.value)
                                                .programViewModels.removeAt(index)
                                            screenModel.pageModels.get(screenModel.currentPageIndex.value)
                                                .programViewModels.add(it)
                                        }
                                        .padding(10.dp)
                                    ) {
                                        val open = remember { mutableStateOf(false) }
                                        Row {
                                            Column(
                                                Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    it.program.appName.first().uppercaseChar() +
                                                            it.program.appName.drop(1),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    fontWeight = FontWeight.Black
                                                )
                                                Text(
                                                    it.program.appId,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            var icon = remember { mutableStateOf(Icons.Filled.KeyboardArrowDown) }
                                            IconButton(onClick = {
                                                if (icon.value == Icons.Filled.KeyboardArrowDown) {
                                                    icon.value = Icons.AutoMirrored.Filled.KeyboardArrowLeft
                                                    open.value = true
                                                } else {
                                                    icon.value = Icons.Filled.KeyboardArrowDown
                                                    open.value = false
                                                }
                                            }) {
                                                Icon(imageVector = icon.value,
                                                    contentDescription = "Close",
                                                    tint = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        val items = remember { it.program.store.data.items }

                                        var modifier = if (open.value) Modifier
                                        else Modifier.height(0.dp)
                                        Column(
                                            modifier = modifier.padding(0.dp, 5.dp, 0.dp, 0.dp).animateContentSize()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                                .padding(5.dp).fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(5.dp),
                                        ) {
                                            items.forEachIndexed { index, item ->
                                                Column{
                                                    Row {
                                                        Column(Modifier.weight(1f)) {
                                                            Text(
                                                                item.name,
                                                                color = MaterialTheme.colorScheme.onSurface,
                                                                fontWeight = FontWeight.Black
                                                            )
                                                            Text(
                                                                item.value.toString(),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                        IconButton(onClick = {
                                                            it.program.store.data.items.removeAt(index)
                                                        }) {
                                                            Icon(
                                                                imageVector = Icons.Filled.Delete,
                                                                contentDescription = "Close",
                                                                tint = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        }
                                                    }
                                                    HorizontalDivider()
                                                }
                                            }
                                        }

                                    }
                                }
                        }
                    }
                    Column(
                        Modifier
                            .padding(5.dp,5.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .fillMaxSize()
                    ) {

                        var scrollState = rememberScrollState(0)
                        Column(
                            Modifier.verticalScroll(scrollState)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(10.dp).weight(1f).animateContentSize().fillMaxWidth()
                        ) {
                            Text("All programs",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Black, fontSize = 25.sp, modifier = Modifier.padding(10.dp))
                            Programs.programs.forEach {
                                    Column (Modifier.padding(10.dp,5.dp).fillMaxWidth()
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .clickable {
                                            VirtelSystem.appsScreen.value = false
                                            Programs.startProgram(it.appId)
                                        }.padding(10.dp)
                                    ) {
                                        Text(it.appName.first().uppercaseChar()+
                                                it.appName.drop(1),
                                            color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Black)
                                        Text(it.appId,color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                        }
                    }
                    Column(
                        Modifier
                            .padding(5.dp,5.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .fillMaxSize()
                    ) {
                        var scrollState = rememberScrollState(0)
                        Column(
                            Modifier.verticalScroll(scrollState)
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(20.dp).weight(1f).animateContentSize(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Logger.logs.forEach {
                                Text(text = it, color = MaterialTheme.colorScheme.onSurface)
                            }
                            LaunchedEffect(Logger.logs.size) {
                                scrollState.animateScrollTo(Int.MAX_VALUE)
                            }

                        }
                        Row(
                            Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            var value by remember { mutableStateOf("") }
                            TextField(
                                modifier = Modifier.weight(1f),
                                label = { Text("Write command") },
                                supportingText = { Text("Steps. Example: csl write \"Hello world\";") },
                                prefix =
                                { if(Logger.readLine.value){
                                    Text("#")
                                } else {
                                    Text(">")
                                }},
                                value = value, onValueChange = { value = it }
                            )
                            Button(modifier = Modifier.padding(10.dp,0.dp),
                                onClick = {
                                    CoroutineScope(Job()).launch {
                                        if(Logger.readLine.value){
                                            Logger.readLine.value = false
                                            Logger.afterReadLine(value)
                                        } else {
                                            var program = Program("")
                                            program.appId = "vladceresna.virtel.console"
                                            program.appName = "Console"
                                            program.status = ProgramStatus.BACKGROUND
                                            var flow = Flow(program, "main", Step(false))
                                            flow.runCode(value, "/start.steps")
                                        }
                                    }
                            }){ Text("Send") }
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