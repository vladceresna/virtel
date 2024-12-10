package com.vladceresna.virtel.other

import com.vladceresna.virtel.controllers.Program
import com.vladceresna.virtel.screens.model.ProgramViewModel

class VirtelException(override val message: String?) : Exception()

fun makeError(programModel:ProgramViewModel, errorMessage:String){
    programModel.errorMessage.value = errorMessage
    programModel.isErrorHappened.value = true
}