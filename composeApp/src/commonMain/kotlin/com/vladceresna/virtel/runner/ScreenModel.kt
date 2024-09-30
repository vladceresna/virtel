package com.vladceresna.virtel.runner

data class ScreenModel (
    var widgetModels: List<WidgetModel> = emptyList()
){
}

data class WidgetModel(
    var widgetModels: List<WidgetModel> = emptyList(),

){

}