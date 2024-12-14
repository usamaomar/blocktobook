package com.example.plugins

import com.google.api.RoutingProto.routing
import io.ktor.serialization.jackson.jackson
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
        })
        jackson()
        routing
    }
}
