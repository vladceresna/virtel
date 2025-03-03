package com.vladceresna.virtel.screens.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.terrakok.adaptivestack.HorizontalAdaptiveStack
import com.vladceresna.virtel.controllers.FileSystem
import com.vladceresna.virtel.controllers.Flow
import com.vladceresna.virtel.controllers.Logger
import com.vladceresna.virtel.controllers.Program
import com.vladceresna.virtel.controllers.ProgramStatus
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.Step
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.screens.model.ScreenModel

import dev.snipme.highlights.model.SyntaxThemes
import com.vladceresna.virtel.lib.kodeview.CodeEditText
import dev.snipme.highlights.Highlights
import dev.snipme.highlights.model.SyntaxLanguage
import dev.snipme.highlights.model.SyntaxTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okio.Path.Companion.toPath
import org.jetbrains.compose.resources.vectorResource
import virtel.composeapp.generated.resources.Res
import virtel.composeapp.generated.resources.baseline_close_fullscreen_24

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(
    modifier:Modifier = Modifier,
    screenModel: ScreenModel
){


    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HorizontalAdaptiveStack {
            var running by remember { mutableStateOf(true) }
            var all by remember { mutableStateOf(true) }
            var terminal by remember { mutableStateOf(true) }

            var runningModifier: Modifier = Modifier.weight(1f).fillMaxSize()
            var allModifier: Modifier = Modifier.weight(1f).fillMaxSize()
            var terminalModifier: Modifier = Modifier.weight(1f).fillMaxSize()


            if(running) {
                Column(
                    runningModifier
                        .padding(5.dp, 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {

                    var scrollState = rememberScrollState(0)
                    Column(
                        Modifier.verticalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(10.dp).weight(1f).animateContentSize().fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Running programs", Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Black, fontSize = 25.sp
                            )
                            IconButton(onClick = {
                                running = true
                                all = !all
                                terminal = !terminal
                            }) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.baseline_close_fullscreen_24),
                                    contentDescription = "Fullscreen",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        screenModel.pageModels.get(screenModel.currentPageIndex.value)
                            .programViewModels.forEachIndexed { index, it ->
                                Column(
                                    Modifier.padding(10.dp, 5.dp).fillMaxWidth()
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
                                        var icon =
                                            remember { mutableStateOf(Icons.Filled.KeyboardArrowDown) }
                                        IconButton(onClick = {
                                            if (icon.value == Icons.Filled.KeyboardArrowDown) {
                                                icon.value =
                                                    Icons.AutoMirrored.Filled.KeyboardArrowLeft
                                                open.value = true
                                            } else {
                                                icon.value = Icons.Filled.KeyboardArrowDown
                                                open.value = false
                                            }
                                        }) {
                                            Icon(
                                                imageVector = icon.value,
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    val items = remember { it.program.flows }

                                    var modifier = if (open.value) Modifier
                                    else Modifier.height(0.dp)
                                    Column(
                                        modifier = modifier.padding(top = 5.dp)
                                            .fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(5.dp),
                                    ) {
                                        items.forEach { (t, u) ->

                                            val openFlow = remember { mutableStateOf(false) }
                                            Column(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                                    .fillMaxWidth().padding(5.dp),
                                            ) {
                                                Row(
                                                    Modifier.clip(RoundedCornerShape(10.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                                        .fillMaxWidth().padding(start = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        t,
                                                        Modifier.weight(1f),
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        fontWeight = FontWeight.Black
                                                    )
                                                    IconButton({
                                                        u.run = false
                                                        items.remove(t)
                                                    }) {
                                                        Icon(
                                                            imageVector = Icons.Filled.Delete,
                                                            contentDescription = "Close",
                                                            tint = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                    var iconInFlow =
                                                        remember { mutableStateOf(Icons.Filled.KeyboardArrowDown) }
                                                    IconButton(onClick = {
                                                        if (iconInFlow.value == Icons.Filled.KeyboardArrowDown) {
                                                            iconInFlow.value =
                                                                Icons.AutoMirrored.Filled.KeyboardArrowLeft
                                                            openFlow.value = true
                                                        } else {
                                                            iconInFlow.value =
                                                                Icons.Filled.KeyboardArrowDown
                                                            openFlow.value = false
                                                        }
                                                    }) {
                                                        Icon(
                                                            imageVector = iconInFlow.value,
                                                            contentDescription = "Close",
                                                            tint = MaterialTheme.colorScheme.onSurface
                                                        )
                                                    }
                                                }
                                                var modifierFlow = if (openFlow.value) Modifier
                                                else Modifier.height(0.dp)
                                                Column(
                                                    modifierFlow.padding(top = 5.dp),
                                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                                ) {
                                                    u.store.data.forEach {
                                                        Column(
                                                            Modifier.clip(RoundedCornerShape(10.dp))

                                                                .fillMaxWidth(),
                                                        ) {
                                                            Row(
                                                                Modifier.clip(
                                                                    RoundedCornerShape(
                                                                        10.dp,
                                                                        10.dp,
                                                                        0.dp,
                                                                        0.dp
                                                                    )
                                                                )
                                                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                                                    .padding(start = 10.dp)
                                                                    .fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    it.key.first,
                                                                    Modifier,
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    fontWeight = FontWeight.Black
                                                                )
                                                                Text(
                                                                    " ( " + it.key.second.toString() + " )",
                                                                    Modifier.weight(1f),
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    fontWeight = FontWeight.W100
                                                                )
                                                                IconButton({
                                                                    u.store.data.remove(it.key)
                                                                }) {
                                                                    Icon(
                                                                        imageVector = Icons.Filled.Delete,
                                                                        contentDescription = "Close",
                                                                        tint = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                }

                                                            }
                                                            Row(
                                                                Modifier.clip(
                                                                    RoundedCornerShape(
                                                                        0.dp,
                                                                        0.dp,
                                                                        10.dp,
                                                                        10.dp
                                                                    )
                                                                )
                                                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                                                    .padding(
                                                                        10.dp,
                                                                        0.dp,
                                                                        10.dp,
                                                                        10.dp
                                                                    )
                                                                    .fillMaxWidth(),
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Text(
                                                                    it.value.toString(),
                                                                    Modifier,
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    fontWeight = FontWeight.W100
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }
                            }
                    }
                }
            }
            if (all) {
                Column(
                    allModifier
                        .padding(5.dp, 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {

                    var scrollState = rememberScrollState(0)
                    Column(
                        Modifier.verticalScroll(scrollState)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(10.dp).weight(1f).animateContentSize().fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "All programs", Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Black, fontSize = 25.sp
                            )
                            IconButton(onClick = {
                                running = !running
                                all = true
                                terminal = !terminal
                            }) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.baseline_close_fullscreen_24),
                                    contentDescription = "Fullscreen",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Programs.programs.forEach {
                            Row(
                                Modifier.padding(10.dp, 5.dp).fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .clickable {
                                        VirtelSystem.appsScreen.value = false
                                        Programs.startProgram(it.appId)
                                    }.padding(10.dp)
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        it.appName.first().uppercaseChar() +
                                                it.appName.drop(1),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Black
                                    )
                                    Text(it.appId, color = MaterialTheme.colorScheme.onSurface)
                                }

                                var showDialog by remember { mutableStateOf(false) }
                                IconButton(onClick = {
                                    showDialog = true
                                }) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (showDialog) {
                                    AlertDialog(
                                        onDismissRequest = {
                                            showDialog = false
                                        },
                                        title = { Text(text = "Really?") },
                                        text = { Text(text = "Do you want delete this app: " + it.appName) },
                                        confirmButton = {
                                            Button(
                                                colors = ButtonColors(
                                                    containerColor = Color.Red,
                                                    contentColor = Color.White,
                                                    disabledContainerColor = Color.Red,
                                                    disabledContentColor = Color.White
                                                ),
                                                onClick = {
                                                    okio.FileSystem.SYSTEM.deleteRecursively(
                                                        Programs.findProgram(it.appId).path.toPath()
                                                    )
                                                    showDialog = false
                                                }
                                            ) {
                                                Text(
                                                    text = "Yes"
                                                )
                                            }
                                        },
                                        dismissButton = {
                                            OutlinedButton(
                                                onClick = {
                                                    showDialog = false
                                                }
                                            ) {
                                                Text(text = "No")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (terminal) {
                Column(
                    terminalModifier
                        .padding(5.dp, 5.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    var scrollState = rememberLazyListState()
                    Row(
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow).padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Terminal", Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Black, fontSize = 25.sp
                        )
                        IconButton(onClick = {
                            running = !running
                            all = !all
                            terminal = true
                        }) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.baseline_close_fullscreen_24),
                                contentDescription = "Fullscreen",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    LazyColumn(
                        state = scrollState,
                        modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(20.dp).animateContentSize().fillMaxWidth(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        items(Logger.logs.size) { index ->
                            Text(text = Logger.logs.get(index), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    LaunchedEffect(Logger.logs.size) {

                        fun isEditTagItemFullyVisible(lazyListState: LazyListState, editTagItemIndex: Int): Boolean {
                            with(lazyListState.layoutInfo) {
                                val editingTagItemVisibleInfo = visibleItemsInfo.find { it.index == editTagItemIndex }
                                return if (editingTagItemVisibleInfo == null) {
                                    false
                                } else {
                                    viewportEndOffset - editingTagItemVisibleInfo.offset >= editingTagItemVisibleInfo.size
                                }
                            }
                        }

                        delay(300)
                        if (!isEditTagItemFullyVisible(scrollState, Logger.logs.size-1)) {
                            scrollState.animateScrollToItem(Logger.logs.size-1)
                        }
                    }

                    Row(
                        Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {

                        var value by remember { mutableStateOf("") }


                        CodeEditText(
                            modifier = Modifier.weight(1f),
                            highlights = Highlights.Builder()
                                .code(value)
                                .theme(
                                    SyntaxTheme(
                                        key = "virtel",
                                        code = 0x00F8C6,
                                        keyword = 0x1FD02B,
                                        string = 0xFFD66D,
                                        literal = 0xD7351D,
                                        comment = 0x67E667,
                                        metadata = 0xD7351D,
                                        multilineComment = 0x67E667,
                                        punctuation = 0xD28080,
                                        mark = 0xD28080
                                    )
                                )
                                .language(SyntaxLanguage.STEPS)
                                .build(),
                            label = { Text("Write command") },
                            onValueChange = { value = it },
                            supportingText = { Text("Steps. Ex.: csl write \"Hello world\";") },
                            prefix = {
                                if (Logger.readLine.value) {
                                    Text("#")
                                } else {
                                    Text(">")
                                }
                            }
                        )


                        Button(modifier = Modifier.padding(10.dp, 0.dp),
                            onClick = {
                                CoroutineScope(Job()).launch {
                                    if (Logger.readLine.value) {
                                        Logger.readLine.value = false
                                        Logger.afterReadLine(value)
                                    } else {
                                        var program = Program(
                                            FileSystem.programsPath +
                                                    "/vladceresna.virtel.terminal"
                                        )
                                        program.appId = "vladceresna.virtel.terminal"
                                        program.appName = "Terminal"
                                        program.status = ProgramStatus.BACKGROUND
                                        program.setStorage()
                                        var flow = Flow(program, "main", Step(false))
                                        flow.runCode(value, "/start.steps")
                                        program.storage.stop()
                                    }
                                }
                            }
                        ) { Text("Send") }
                    }
                }
            }
        }
    }
}