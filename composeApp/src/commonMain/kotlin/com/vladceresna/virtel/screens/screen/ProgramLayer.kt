package com.vladceresna.virtel.screens.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vladceresna.virtel.controllers.VirtelSystem
import com.vladceresna.virtel.icons.X
import com.vladceresna.virtel.screens.Widget
import com.vladceresna.virtel.screens.model.ProgramViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramLayer(
    programViewModel:ProgramViewModel,modifier: Modifier
){

        Scaffold(
            modifier.fillMaxSize(),
            topBar = {
                Box {
                    TopAppBar(
                        modifier = Modifier,
                        title = {
                            Text(
                                programViewModel.program.appName.first().uppercaseChar() +
                                        programViewModel.program.appName.drop(1),
                                fontWeight = FontWeight.Black
                            )
                        },
                        actions = {

                            IconButton(onClick = {
                                programViewModel.program.storage.stop()
                                programViewModel.program.flows.forEach { (t, u) -> u.run = false }
                                programViewModel.program.flows.clear()
                                VirtelSystem.screenModel
                                    .pageModels[VirtelSystem.screenModel.currentPageIndex.value]
                                    .programViewModels.remove(programViewModel)
                                //TODO: need to stop program work
                            }) {
                                Icon(
                                    imageVector = X,
                                    contentDescription = "close",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    )
                }
            },
            bottomBar = {
                var bottomData = programViewModel.widgets.find {
                    it.name == "bottom"
                }
                if(!bottomData!!.childs.isEmpty()){
                    var model = bottomData
                    Widget(model, Modifier)
                }
            }
        ) {
            if (!programViewModel.isErrorHappened.value) {
                Column (
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(
                        10.dp, 64.dp, 10.dp,
                        if (!programViewModel.widgets.find {
                                it.name == "bottom"
                            }!!.childs.isEmpty()) 90.dp else 10.dp
                    )
                ) {
                    var rootData = programViewModel.widgets.find {
                        it.name == "root"
                    }

                    if (!rootData!!.childs.isEmpty()) {
                        var model = rootData
                        Widget(model, Modifier)

                    }
                }
            } else {


                AlertDialog(
                    onDismissRequest = {
                        programViewModel.isErrorHappened.value = false
                    },
                    title = { Text(text = "Something went wrong") },
                    text = { Text(text = programViewModel.errorMessage.value+
                                "\n\nPlease contact the developer of this program " +
                            "and provide him with the error text described above") },
                    confirmButton = { // 6
                        Button(
                            onClick = {
                                programViewModel.isErrorHappened.value = false
                                println("Restarting")
                            }
                        ) {
                            Text(
                                text = "Restart"
                            )
                        }
                    }
                )
            }
        }

}