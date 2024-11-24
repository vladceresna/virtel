package com.vladceresna.virtel.screens.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.screens.model.PageModel

@Composable
fun Page(
    pageModel: PageModel,
    modifier: Modifier
) {
    Column (
        Modifier.fillMaxSize()
    ){
        Box (modifier = modifier){
            if(pageModel.programViewModels.size>0) {
                ProgramLayer(pageModel.programViewModels.last(), Modifier.fillMaxSize())
            } else {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(150.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircle,
                            contentDescription = "Plus",
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }
        }
        Row {
            Text("", Modifier.weight(1f))
            IconButton(onClick = {
                try {
                    pageModel.settingsClick.value()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Modifier.weight(1f)) {
                Icon(Icons.Filled.Settings, contentDescription = "Localized description")
            }
            IconButton(onClick = {
                try {
                    pageModel.homeClick.value()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Modifier.weight(1f)) {
                Icon(Icons.Filled.Home, contentDescription = "Localized description")
            }
            IconButton(onClick = {
                try {
                    pageModel.backClick.value()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Modifier.weight(1f)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Localized description"
                )
            }
            Text("", Modifier.weight(1f))
        }
    }
}