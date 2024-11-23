package com.vladceresna.virtel.screens.model

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.controllers.Program
import com.vladceresna.virtel.controllers.Programs


data class ScreenModel(
    var pageModels: MutableList<PageModel> = mutableListOf(PageModel()),
    var currentPageIndex:Int = 0
)
data class PageModel(
    var settingsClick:() -> Unit = {

    },
    var homeClick:() -> Unit = {

    },
    var backClick:() -> Unit = {

    }
){
    var programViewModels: MutableList<ProgramViewModel> =
        mutableListOf()
}
data class ProgramViewModel(
    var pageModel: PageModel,
    var program: Program
) {
    var isErrorHappened = false
    var errorMessage = ""

    var widgets:MutableList<WidgetModel> = mutableListOf(
        WidgetModel(this,"root", WidgetType.COLUMN),
        WidgetModel(this, "top", WidgetType.TOP_BAR),
        WidgetModel(this, "bottom",WidgetType.BOTTOM_BAR)
    )
}




data class WidgetModel(
    var programViewModel: ProgramViewModel,
    var name:String,
    var widgetType: WidgetType = WidgetType.VIEW,
    var weight:Float = 1F, // float
    var variant:String = "primary", // primary/secondary/tertiary/destructive
    var title:String = "",
    var value:String = "",
    var foreground:String = "",
    var background:String = "",
    var onClick:String = "",
    var paddingTop:Dp = 0.dp,
    var paddingRight:Dp = 0.dp,
    var paddingBottom:Dp = 0.dp,
    var paddingLeft:Dp = 0.dp,
    var marginTop:Dp = 0.dp,
    var marginRight:Dp = 0.dp,
    var marginBottom:Dp = 0.dp,
    var marginLeft:Dp = 0.dp,
    var scrollable:Boolean = false,

    var childs:MutableList<WidgetModel> = mutableListOf(),
)

enum class WidgetType{
    VIEW, ADAPTIVE, COLUMN, ROW, BUTTON, TEXT, IMAGE, INPUT, CARD, TOP_BAR, BOTTOM_BAR
}