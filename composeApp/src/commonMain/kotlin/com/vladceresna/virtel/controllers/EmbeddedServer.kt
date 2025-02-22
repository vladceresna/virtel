package com.vladceresna.virtel.controllers

data class EmbeddedServer(
    var routes:MutableSet<Route> = mutableSetOf(),
    var stopFuns:HashMap<Int,() -> Unit> = hashMapOf()
)

data class Route(
    var route: String = "/",
    var method: String = "get",
    var file: String = "/slash.steps",
    var resVar: String = "resVar"
)