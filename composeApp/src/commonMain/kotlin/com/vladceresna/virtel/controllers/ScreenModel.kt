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

data class WidgetModel(
    var childs: MutableList<WidgetModel> = mutableListOf(),
    var widgetType: WidgetType = WidgetType.VIEW
) {

}

enum class WidgetType{
    VIEW, COLUMN, ROW, BUTTON, TEXT, INPUT, TOP_BAR, BOTTOM_BAR
}