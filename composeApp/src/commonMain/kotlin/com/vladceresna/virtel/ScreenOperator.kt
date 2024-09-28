package com.vladceresna.virtel

object ScreenOperator {
    var screen:Screen = Screen("Hel")
}

data class Screen (
    var appId:String
)