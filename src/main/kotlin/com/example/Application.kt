package com.example

import com.example.plugins.*
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.cio.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    firebaseInit()
    configureCORS()
    intercept()
    configureSerialization()
    configureRouting()
    configureMonitoring()
}
