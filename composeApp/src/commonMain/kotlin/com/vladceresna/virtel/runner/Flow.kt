package com.vladceresna.virtel.runner

class Flow {
    fun cmdWrite(args: List<String>){

    }
    fun cmdRead(args: List<String>){

    }
    fun runStep(step: Step){
        when(step.mod){
            "cmd" -> when(step.cmd){
                "write" -> cmdWrite(step.args)
                "read" -> cmdRead(step.args)
            }
        }
    }
    //todo: read file and split by lines
}

data class Step(
    val mod: String,
    val cmd: String,
    var args: List<String>
)