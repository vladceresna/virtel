package com.vladceresna.virtel.screens.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.controllers.ScreenModel
import com.vladceresna.virtel.screens.model.PageModel

@Composable
fun Page(
    pageModel: PageModel,
    modifier: Modifier = Modifier.fillMaxSize()
        .clip(RoundedCornerShape(4.dp))
) {
    Box(
        modifier
    ) {
        pageModel.programViewModels.forEach {
            ProgramLayer(it)
        }
    }
    Row {
        Text("", Modifier.weight(1f))
        IconButton(onClick = { try{
            ScreenModel.settingsClick()}catch (e:Exception){e.printStackTrace()} }, Modifier.weight(1f)) {
            Icon(Icons.Filled.Settings, contentDescription = "Localized description")
        }
        IconButton(onClick = { try{
            ScreenModel.homeClick()}catch (e:Exception){e.printStackTrace()} }, Modifier.weight(1f)) {
            Icon(Icons.Filled.Home, contentDescription = "Localized description")
        }
        IconButton(onClick = { try{
            ScreenModel.backClick()}catch (e:Exception){e.printStackTrace()} }, Modifier.weight(1f)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Localized description")
        }
        Text("", Modifier.weight(1f))
    }
}