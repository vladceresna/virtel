package com.vladceresna.virtel.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.log
import com.vladceresna.virtel.other.makeError
import com.vladceresna.virtel.screens.model.WidgetModel
import com.vladceresna.virtel.screens.model.WidgetType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Widget(model: WidgetModel, modifier: Modifier){


    log("Render Widget $model", Log.DEBUG)
    var arrangementVertical = Arrangement.Center
    var arrangementHorizontal = Arrangement.Center
    var alignmentVertical = Alignment.CenterVertically
    var alignmentHorizontal = Alignment.CenterHorizontally


    var modifierClickable = modifier


    if(model.scrollable.value){
        var scrollState = rememberScrollState()
        when(model.widgetType){
            WidgetType.COLUMN -> modifierClickable = modifierClickable.verticalScroll(scrollState)
            WidgetType.ROW -> modifierClickable = modifierClickable.horizontalScroll(scrollState)
            else -> makeError(model.programViewModel,
                "This WidgetType doesnt support scroll: "+model.widgetType+
                        " in var: "+model.name)
        }
    }

    modifierClickable = modifierClickable
        .padding(
            model.marginLeft.value,
            model.marginTop.value,
            model.marginRight.value,
            model.marginBottom.value,
        )
        .padding(
            model.paddingLeft.value,
            model.paddingTop.value,
            model.paddingRight.value,
            model.paddingBottom.value,
        )



    var onClick: () -> Unit = {}
    if (!model.onClick.value.equals("")){
        modifierClickable = modifier.clickable {
            model.programViewModel.program.runFlow(model.onClick.value, model.onClick.value) }
        onClick = {
            model.programViewModel.program.runFlow(model.onClick.value, model.onClick.value) }
    }
    println("${model.widgetType} ${model.title.value}")
    when (model.widgetType) {
        WidgetType.TEXT -> {
            when(model.variant.value) {
                "primary" -> {
                    Text(
                        modifier = modifierClickable,
                        text = model.title.value,
                        fontSize = TextUnit(40f, TextUnitType.Sp),
                        lineHeight = TextUnit(50f, TextUnitType.Sp),
                        fontWeight = FontWeight.Bold
                    )
                }
                else -> Text(
                    modifier = modifierClickable,
                    text = model.title.value,
                    fontSize = TextUnit(20f, TextUnitType.Sp)
                )
            }
        }
        WidgetType.BUTTON -> when(model.variant.value) {
            "primary" ->
                    Button(onClick = onClick, modifier = modifierClickable) {

                            if(model.childs.size == 1){
                                SizedWidget(
                                    model.childs.get(0),
                                    Modifier
                                )
                            }

                    }

        }
        WidgetType.INPUT -> {
            Box(modifierClickable) {
                var mutValue by remember { mutableStateOf(model.value.value) }
                OutlinedTextField(
                    label = { Text(text = model.title.value) },
                    modifier = modifierClickable,
                    value = mutValue,
                    onValueChange = {
                        mutValue = it
                        model.value.value = mutValue
                    },
                )
            }
        }
        WidgetType.ROW -> Box(modifierClickable) {
            Row(
                modifier = modifierClickable,
                horizontalArrangement = arrangementHorizontal,
                verticalAlignment = alignmentVertical
            ) {
                model.childs.forEach {
                    SizedWidget(it,
                        if(it.weight.value != 0f) {
                            Modifier.weight(it.weight.value)
                        } else Modifier
                    )
                }
            }
        }
        WidgetType.COLUMN -> Box(modifierClickable) {
            Column(
                modifier = modifierClickable,
                verticalArrangement = arrangementVertical,
                horizontalAlignment = alignmentHorizontal
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
        }
        WidgetType.ADAPTIVE -> {
            if(model.childs.size == 2) {
                Box(modifierClickable) {
                    Adaptive(
                        leftModel = model.childs.get(0),
                        rightModel = model.childs.get(1)
                    )
                }
            }
        }
        WidgetType.CARD -> {
            ElevatedCard(modifierClickable,
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                ) {
                Column(modifierClickable) {
                    model.childs.forEach {
                        SizedWidget(
                            it,
                            if (it.weight.value != 0f) {
                                Modifier.weight(it.weight.value)
                            } else Modifier
                        )
                    }
                }
            }
        }
        WidgetType.TOP_BAR -> TopAppBar(
            title = {
                Text("I'm a TopAppBar")
            },
            navigationIcon = {
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.ArrowBack, null)
                }
            }, actions = {
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.Share, null)
                }
                IconButton(onClick = {/* Do Something*/ }) {
                    Icon(Icons.Filled.Settings, null)
                }
            }
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