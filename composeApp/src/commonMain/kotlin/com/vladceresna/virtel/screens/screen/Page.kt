package com.vladceresna.virtel.screens.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.controllers.Programs
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.screens.model.PageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Page(
    pageModel: PageModel,
    modifier: Modifier, pager: PagerState
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
                        onClick = {
                            Programs.startProgram("vladceresna.virtel.launcher")
                        },
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(150.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddCircle,
                            contentDescription = "Plus",
                            modifier = Modifier.size(150.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(
                        onClick = {
                            if(VirtelSystem.screenModel.pageModels.size > 1) {
                                VirtelSystem.screenModel.pageModels
                                    .removeAt(VirtelSystem.screenModel.currentPageIndex.value)
                            }
                        },
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(150.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(150.dp),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
        Row {
            IconButton(onClick = {
                if(pager.currentPage != 0) {
                    runBlocking {
                        pager.scrollToPage(pager.currentPage - 1)
                    }
                }
            }, Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = {
                try {
                    pageModel.settingsClick.value()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Modifier.weight(1f)) {
                Icon(Icons.Filled.Settings, contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onBackground)
            }
            IconButton(onClick = {
                try {
                    pageModel.homeClick.value()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, Modifier.weight(1f)) {
                Icon(Icons.Filled.Home, contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onBackground)
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
                    contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(
                onClick = {
                if(pager.currentPage < pager.pageCount - 1) {
                    runBlocking {
                        pager.scrollToPage(pager.currentPage + 1)
                    }
                }
            }, Modifier.weight(1f)) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Localized description",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}