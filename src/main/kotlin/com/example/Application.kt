package com.example

import com.example.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    firebaseInit()
    configureCORS()
    intercept()
    configureKoin()
    configureSerialization()
    configureRouting()
    configureMonitoring()
}
