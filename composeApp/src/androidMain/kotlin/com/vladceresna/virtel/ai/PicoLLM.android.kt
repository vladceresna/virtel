package com.vladceresna.virtel.ai

import ai.picovoice.picollm.PicoLLM
import ai.picovoice.picollm.PicoLLMCompletion
import ai.picovoice.picollm.PicoLLMException
import ai.picovoice.picollm.PicoLLMGenerateParams
import com.vladceresna.virtel.controllers.VirtelSystem


data object PicoLLMOperator {
    lateinit var pllm:PicoLLM

    var messages: MutableList<String> = mutableListOf()

    fun setupPicoLLM(){
        messages = mutableListOf()
        pllm = PicoLLM.Builder()
            .setAccessKey(VirtelSystem.pico)
            .setModelPath(VirtelSystem.llm)
            .build()
    }


}


actual fun giveAnswer(message: String): String {
    val res: PicoLLMCompletion
    try {
        PicoLLMOperator.setupPicoLLM()
        PicoLLMOperator.messages.add(message)

        var prompt = ""
        PicoLLMOperator.messages.forEach {
            prompt+=it+"\n\n"
        }

        res = PicoLLMOperator.pllm.generate(
            prompt,
            PicoLLMGenerateParams.Builder().build()
        )
        PicoLLMOperator.pllm.delete()
        return res.completion
    } catch (e: PicoLLMException) {
    }
    return ""
}