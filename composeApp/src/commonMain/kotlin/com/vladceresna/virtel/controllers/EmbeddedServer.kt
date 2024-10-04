package com.vladceresna.virtel.controllers

data class EmbeddedServer(
    var port:String = "8080",
    var routes:MutableSet<Route> = mutableSetOf()
)

data class Route(
    var route: String = "/",
    var method: String = "get",
    var file: String = "/slash.steps",
    var resVar: String = "resVar"
)