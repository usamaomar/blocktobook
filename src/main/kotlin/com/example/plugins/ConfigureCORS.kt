package com.example.plugins


import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCORS() {
    install(CORS) {
        this.methods.add(HttpMethod.Options)
        this.methods.add(HttpMethod.Get)
        this.methods.add(HttpMethod.Post)
        this.methods.add(HttpMethod.Put)
        this.methods.add(HttpMethod.Delete)
        this.headers.add(HttpHeaders.Authorization)
        this.headers.add(HttpHeaders.ContentType)
        allowCredentials = true
        anyHost() // For development, you can use this. But in production, specify only allowed domains.
        maxAgeInSeconds = 3600 // Cache preflight requests for 1 hour

    }
}
