package com.vladceresna.virtel.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vladceresna.virtel.screens.model.VirtelScreenViewModel
import com.vladceresna.virtel.screens.screen.ScreenPager


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VirtelScreen(
    screenModel: VirtelScreenViewModel = viewModel<VirtelScreenViewModel>()
) {
   ScreenPager(screenModel.screenModel)
}
