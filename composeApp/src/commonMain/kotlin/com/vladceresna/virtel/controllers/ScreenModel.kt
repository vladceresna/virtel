package com.vladceresna.virtel.controllers

data object ScreenModel {
    var root:String = ""
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

    var width = 50//fill/wrap/fixed
    var height = 50
    var weight:Float = 1f
    var variant = 0//primary/secondary/tertiary/destructive
    var title = "Label"
    var value = ""
    var onclick = "noclick.steps"
    var isRoot = false
}

enum class WidgetType{
    VIEW, COLUMN, ROW, BUTTON, TEXT, INPUT, TOP_BAR, BOTTOM_BAR
}