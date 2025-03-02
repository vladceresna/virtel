package com.vladceresna.virtel.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key.Companion.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.github.terrakok.adaptivestack.HorizontalAdaptiveStack
import com.vladceresna.virtel.controllers.DataType
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.controllers.log
import com.vladceresna.virtel.other.VirtelException
import com.vladceresna.virtel.other.makeError
import com.vladceresna.virtel.screens.model.WidgetModel
import com.vladceresna.virtel.screens.model.WidgetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import sun.swing.SwingUtilities2.drawRect
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Widget(model: WidgetModel, modifier: Modifier){
    //log("Render Widget $model", Log.DEBUG)

    var arrangementVertical:Arrangement.Vertical = Arrangement.Center
    var arrangementHorizontal:Arrangement.Horizontal = Arrangement.Center
    var alignmentVertical = Alignment.CenterVertically
    var alignmentHorizontal = Alignment.CenterHorizontally





    if (model.align.value == "center") {
        alignmentVertical = Alignment.CenterVertically
        alignmentHorizontal = Alignment.CenterHorizontally
    } else if (model.align.value == "start") {
        alignmentVertical = Alignment.Top
        alignmentHorizontal = Alignment.Start
    } else if (model.align.value == "end") {
        alignmentVertical = Alignment.Bottom
        alignmentHorizontal = Alignment.End
    }

    if (model.justify.value == "center") {
        arrangementVertical = Arrangement.Center
        arrangementHorizontal = Arrangement.Center
    } else if (model.justify.value == "start") {
        arrangementVertical = Arrangement.Top
        arrangementHorizontal = Arrangement.Start
    } else if (model.justify.value == "end") {
        arrangementVertical = Arrangement.Bottom
        arrangementHorizontal = Arrangement.End
    } else if (model.justify.value == "spaceBetween") {
        arrangementVertical = Arrangement.SpaceBetween
        arrangementHorizontal = Arrangement.SpaceBetween
    } else if (model.justify.value == "spaceAround") {
        arrangementVertical = Arrangement.SpaceAround
        arrangementHorizontal = Arrangement.SpaceAround
    } else if (model.justify.value == "spaceEvenly") {
        arrangementVertical = Arrangement.SpaceEvenly
        arrangementHorizontal = Arrangement.SpaceEvenly
    }



    var modifier = modifier


    if(model.scrollable.value){
        var scrollState = rememberScrollState()

        var changes by remember { mutableStateOf(false) }

        LaunchedEffect(scrollState.isScrollInProgress) {
            CoroutineScope(Job()).launch {
                if (!scrollState.isScrollInProgress) {
                    model.scrollableState.value = scrollState.value
                    changes = false
                } else {
                    changes = true
                }
            }
        }
        SideEffect {
            CoroutineScope(Job()).launch {
                if (!scrollState.isScrollInProgress){
                    while (changes) {}
                    scrollState.scrollTo(model.scrollableState.value)
                }
            }
        }

        when(model.widgetType){
            WidgetType.COLUMN -> modifier = modifier.verticalScroll(scrollState)
            WidgetType.ROW -> modifier = modifier.horizontalScroll(scrollState)
            else -> makeError(model.programViewModel,
                "This WidgetType doesnt support scroll: "+model.widgetType+
                        " in var: "+model.name)
        }
    }

    modifier = modifier.padding(
        model.marginLeft.value,
        model.marginTop.value,
        model.marginRight.value,
        model.marginBottom.value
    )

    modifier = modifier.drawBehind {
        drawRect(Color.Transparent)
    }

    if (model.widgetType == WidgetType.CARD) {
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    }


    var onClick: () -> Unit = {}
    if (!model.onClick.value.equals("")){
        modifier = modifier.clickable {
            try {
                var (flowName, thr) = model.programViewModel.program.runFlow(model.onClick.value)
                var flow = model.programViewModel.program.flows.get(flowName)
                    ?: throw VirtelException("Virtel Error: Flow not found: " + flowName)
                flow.nPutVar(
                    "viewName",
                    DataType.VAR,
                    model.name
                )
                thr.start()
            } catch (e: Exception) {
                makeError(model.programViewModel, e.message.toString())
            }
        }
        onClick = {
            try {
                var (flowName, thr) = model.programViewModel.program.runFlow(model.onClick.value)
                var flow = model.programViewModel.program.flows.get(flowName)
                    ?: throw VirtelException("Virtel Error: Flow not found: " + flowName)
                flow.nPutVar(
                    "viewName",
                    DataType.VAR,
                    model.name
                )
                thr.start()
            } catch (e: Exception) {
                makeError(model.programViewModel, e.message.toString())
            }
        }
    }

    modifier = modifier.padding(
        model.paddingLeft.value,
        model.paddingTop.value,
        model.paddingRight.value,
        model.paddingBottom.value
    )



    var asyncImageModel = if (model.value.value.startsWith("http")) {
        model.value.value
    } else {
        File(model.value.value)
    }





    //println("${model.widgetType} ${model.title.value}")
    when (model.widgetType) {
        WidgetType.TEXT -> {
            when(model.variant.value) {
                "super" -> Text(
                    modifier = modifier,
                    text = model.title.value,
                    fontSize = TextUnit(60f, TextUnitType.Sp),
                    lineHeight = TextUnit(70f, TextUnitType.Sp),
                    fontWeight = FontWeight.Black
                )
                "primary" -> Text(
                    modifier = modifier,
                    text = model.title.value,
                    fontSize = TextUnit(40f, TextUnitType.Sp),
                    lineHeight = TextUnit(50f, TextUnitType.Sp),
                    fontWeight = FontWeight.Black
                )
                "secondary" -> Text(
                    modifier = modifier,
                    text = model.title.value,
                    fontSize = TextUnit(30f, TextUnitType.Sp),
                    lineHeight = TextUnit(40f, TextUnitType.Sp),
                    fontWeight = FontWeight.Black
                )
                "tertiary" -> Text(
                    modifier = modifier,
                    text = model.title.value,
                    fontSize = TextUnit(20f, TextUnitType.Sp)
                )
            }
        }
        WidgetType.BUTTON -> when(model.variant.value) {
            "primary" -> Button(onClick = onClick, modifier = modifier) {
                if(model.childs.size == 1){
                    SizedWidget(
                        model.childs.get(0),
                        Modifier
                    )
                }
            }
            "secondary" -> OutlinedButton(onClick = onClick, modifier = modifier) {
                if(model.childs.size == 1){
                    SizedWidget(
                        model.childs.get(0),
                        Modifier
                    )
                }
            }
        }
        WidgetType.INPUT -> Box {
            var mutValue by remember { model.value }
            OutlinedTextField(
                label = { Text(text = model.title.value) },
                modifier = modifier,
                value = mutValue,
                onValueChange = {
                    mutValue = it
                    model.value.value = it
                }
            )
        }

        WidgetType.ROW -> Row(
            modifier,
            arrangementHorizontal,
            alignmentVertical
        ) {
            model.childs.forEach {
                SizedWidget(it,
                    if(it.weight.value != 0f) {
                        Modifier.weight(it.weight.value)
                    } else Modifier
                )
            }
        }
        WidgetType.COLUMN -> Column(
            modifier,
            arrangementVertical,
            alignmentHorizontal
        ) {
            model.childs.forEach {
                SizedWidget(
                    it,
                    if (it.weight.value != 0f) {
                        Modifier.weight(it.weight.value)
                    } else Modifier
                )
            }
        }
        WidgetType.ADAPTIVE -> HorizontalAdaptiveStack(
            modifier,
            arrangementHorizontal,
            alignmentVertical
        ) {
            model.childs.forEach {
                SizedWidget(
                    it,
                    if (it.weight.value != 0f) {
                        Modifier.weight(it.weight.value)
                    } else Modifier
                )
            }
        }
        WidgetType.CARD -> Column (
            modifier,
            arrangementVertical,
            alignmentHorizontal
        ) {
            model.childs.forEach {
                SizedWidget(
                    it,
                    if (it.weight.value != 0f) {
                        Modifier.weight(it.weight.value)
                    } else Modifier
                )
            }
        }
        WidgetType.IMAGE -> AsyncImage(
            modifier = modifier,
            model = asyncImageModel,
            contentDescription = "Image"
        )
        WidgetType.BOTTOM_BAR -> BottomAppBar (
            actions = {
                model.childs.forEach {
                    IconButton(onClick = {  }) {
                        Icon(IconRenderer(model.foreground.value), contentDescription = "Localized description")
                    }
                }
            },
            floatingActionButton = {
                Column {
                    FloatingActionButton(
                        onClick = {  },
                        containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                        elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
                    ) {
                        Icon(Icons.Filled.Menu, "Localized description")
                    }
                }
            }
        )
        else -> Box(modifier)
    }

}
@Composable
fun IconRenderer(name: String):ImageVector{
    return when(name){
        "check" -> Icons.Filled.Check
        else -> Icons.Filled.Menu
    }
}

@Composable
expect fun Adaptive(
    modifier: Modifier,
    leftModel: WidgetModel,
    rightModel: WidgetModel
)



@Composable
fun SizedWidget(model: WidgetModel, modifier: Modifier) {
    var size = model.size.value.split("/")

    var modifier = when (size[0]) {
        "min" -> modifier.wrapContentWidth()
        "max" -> modifier.fillMaxWidth()
        else -> modifier.width(size[0].toInt().dp)
    }

    modifier = when (size[1]) {
        "min" -> modifier.wrapContentHeight()
        "max" -> modifier.fillMaxHeight()
        else -> modifier.height(size[1].toInt().dp)
    }

    Widget(model, modifier)

}