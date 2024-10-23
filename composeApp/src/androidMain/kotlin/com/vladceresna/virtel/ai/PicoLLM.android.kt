package com.vladceresna.virtel.ai

import ai.picovoice.picollm.PicoLLM
import ai.picovoice.picollm.PicoLLMCompletion
import ai.picovoice.picollm.PicoLLMException
import ai.picovoice.picollm.PicoLLMGenerateParams
import com.vladceresna.virtel.controllers.VirtelSystem
import kotlinx.coroutines.runBlocking


data object PicoLLMOperator {
    lateinit var pllm:PicoLLM

    var messages: MutableList<String> = mutableListOf()

    fun setupPicoLLM(){
        println(VirtelSystem.pico)
        println(VirtelSystem.llm)
        messages = mutableListOf()
        pllm = PicoLLM.Builder()
            .setAccessKey(VirtelSystem.pico)
            .setModelPath(VirtelSystem.llm)
            .build()
    }


}


actual fun giveAnswer(message: String): String {
    var completed = false
    var result = ""
    runBlocking {
        val res: PicoLLMCompletion
        try {

            PicoLLMOperator.setupPicoLLM()
            PicoLLMOperator.messages.add(message)

            var prompt = ""
            PicoLLMOperator.messages.forEach {
                prompt += it + "\n"
            }

            res = PicoLLMOperator.pllm.generate(
                prompt,
                PicoLLMGenerateParams.Builder().build()
            )
            PicoLLMOperator.pllm.delete()
            result = res.completion
            if (res.completion.length>5) completed = true
        } catch (e: PicoLLMException) {
            e.printStackTrace()
        }
    }
    while (!completed) {}
    return result.replace("<eos>","").replace("\\n","").replace("\n","")
}