package com.vladceresna.virtel.screens.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.controllers.Program
import com.vladceresna.virtel.controllers.Programs


data class ScreenModel(
    var pageModels: MutableList<PageModel> = mutableStateListOf(PageModel()),
    var currentPageIndex:MutableState<Int> = mutableStateOf(0)
)
data class PageModel(
    var settingsClick:MutableState<() -> Unit> = mutableStateOf({

    }),
    var homeClick:MutableState<() -> Unit> = mutableStateOf({

    }),
    var backClick:MutableState<() -> Unit> = mutableStateOf({

    })
){
    var programViewModels: MutableList<ProgramViewModel> = mutableStateListOf()
}
data class ProgramViewModel(
    var pageModel: PageModel,
    var program: Program
) {
    var isErrorHappened = mutableStateOf(false)
    var errorMessage = mutableStateOf("")

    var widgets:MutableList<WidgetModel> = mutableStateListOf(
        WidgetModel(this,"root", WidgetType.COLUMN),
        WidgetModel(this, "top", WidgetType.TOP_BAR),
        WidgetModel(this, "bottom",WidgetType.BOTTOM_BAR)
    )
}




data class WidgetModel(
    var programViewModel: ProgramViewModel,
    var name:String,
    var widgetType: WidgetType = WidgetType.VIEW,
    var weight:MutableState<Float> = mutableStateOf(1F), // float
    var variant: MutableState<String> = mutableStateOf(""), // primary/secondary/tertiary/destructive
    var title: MutableState<String> = mutableStateOf(""),
    var value: MutableState<String> = mutableStateOf(""),
    var foreground: MutableState<String> = mutableStateOf(""),
    var background: MutableState<String> = mutableStateOf(""),
    var onClick: MutableState<String> = mutableStateOf(""),
    var paddingTop:MutableState<Dp> = mutableStateOf(0.dp),
    var paddingRight:MutableState<Dp> = mutableStateOf(0.dp),
    var paddingBottom:MutableState<Dp> = mutableStateOf(0.dp),
    var paddingLeft:MutableState<Dp> = mutableStateOf(0.dp),
    var marginTop:MutableState<Dp> = mutableStateOf(0.dp),
    var marginRight:MutableState<Dp> = mutableStateOf(0.dp),
    var marginBottom:MutableState<Dp> = mutableStateOf(0.dp),
    var marginLeft:MutableState<Dp> = mutableStateOf(0.dp),
    var scrollable:MutableState<Boolean> = mutableStateOf(false),
    var size:MutableState<String> = mutableStateOf(""),
    var align:MutableState<String> = mutableStateOf(""),
    var justify:MutableState<String> = mutableStateOf(""),

    var childs:MutableList<WidgetModel> = mutableStateListOf()
)

enum class WidgetType{
    VIEW, ADAPTIVE, COLUMN, ROW, LIST, BUTTON, TEXT, IMAGE, INPUT, CARD, TOP_BAR, BOTTOM_BAR
}