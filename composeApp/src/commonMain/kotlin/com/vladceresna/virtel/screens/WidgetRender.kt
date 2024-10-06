package com.vladceresna.virtel.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vladceresna.virtel.controllers.DataType
import com.vladceresna.virtel.controllers.Log
import com.vladceresna.virtel.controllers.WidgetModel
import com.vladceresna.virtel.controllers.WidgetType
import com.vladceresna.virtel.controllers.log

@Suppress
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Widget(rd:Boolean, model: WidgetModel, modifier: Modifier){
    rd
    log("Render Widget $model", Log.DEBUG)
    when (model.widgetType) {
        WidgetType.TEXT -> Text(model.title,modifier)
        else -> Box(modifier)
    }
    /*if (data.returnType == DataType.VIEW) {
        Widget(rd, model,modifier)
    }*/

}