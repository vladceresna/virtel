package com.vladceresna.virtel.controllers

data object ScreenModel {
    var childs: MutableList<WidgetModel> = mutableListOf()
    var bottomAppBar: WidgetModel = WidgetModel()
    var topAppBar: WidgetModel = WidgetModel()

    var settingsClick:() -> Unit = {

    }
    var homeClick:() -> Unit = {

    }
    var backClick:() -> Unit = {

    }


}

data class WidgetModel(var widgetType: WidgetType = WidgetType.VIEW) {
    var childs: MutableList<String> = mutableListOf()//names/ids

    var width = -2//fill/wrap/fixed
    var height = -2
    var variant = 0//primary/secondary/tertiary/destructive
    var title = "Label"
    var value = ""
    var onclick = "noclick.steps"

}

enum class WidgetType{
    VIEW, COLUMN, ROW, BUTTON, TEXT, INPUT, TOP_BAR, BOTTOM_BAR
}