package com.vladceresna.virtel.screens.model

import com.vladceresna.virtel.controllers.Program


data class ScreenModel(
    var pageModels: MutableList<PageModel> = mutableListOf(),
)
data class PageModel(
    var programViewModels: MutableList<ProgramViewModel> = mutableListOf(),
    var settingsClick:() -> Unit = {

    },
    var homeClick:() -> Unit = {

    },
    var backClick:() -> Unit = {

    }
)
data class ProgramViewModel(
    var program: Program
) {
    var root:WidgetModel = WidgetModel(this)
    var topAppBar:WidgetModel = WidgetModel(this)
    var bottomAppBar:WidgetModel = WidgetModel(this)
}



data class WidgetModel(
    var programViewModel: ProgramViewModel,
    var widgetType: WidgetType = WidgetType.VIEW,
    var appId:String = ""
) {
    var childs = mutableListOf<WidgetModel>()
    var name = ""

    var width = 50//fill/wrap/fixed
    var height = 50
    var weight:Float = 1f
    var variant = 0//primary/secondary/tertiary/destructive
    var title = "Label"
    var value = ""
    var onclick = "noclick.steps"
    var isRoot = false

    var icon = ""

    var actions: MutableList<String> = mutableListOf()

}

enum class WidgetType{
    VIEW, COLUMN, ROW, BUTTON, TEXT, IMAGE, INPUT, TOP_BAR, BOTTOM_BAR
}