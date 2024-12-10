package com.vladceresna.virtel.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.screens.model.PageModel
import com.vladceresna.virtel.screens.model.VirtelScreenViewModel
import com.vladceresna.virtel.screens.screen.ScreenPager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


@Composable
fun VirtelScreen(
    screenModel: VirtelScreenViewModel
) {
    ScreenPager(screenModel.screenModel)
}
