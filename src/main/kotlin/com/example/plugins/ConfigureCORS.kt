package com.example.plugins


import io.ktor.http.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCORS() {
    install(CORS) {
        this.methods.add(HttpMethod.Options)
//        this.methods.add(HttpMethod.Get)
//        this.methods.add(HttpMethod.Post)
        this.methods.add(HttpMethod.Put)
//        this.methods.add(HttpMethod.Delete)
//        this.headers.add(HttpHeaders.Authorization)
//        this.headers.add(HttpHeaders.ContentType)
//        allowCredentials = true
//        anyHost() // For development, you can use this. But in production, specify only allowed domains.

        allowHost("https://hayyakblocktobook.com", schemes = listOf("https"))
        allowHost("https://blocktobock-b659b03d852f.herokuapp.com", schemes = listOf("https"))
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
//        allowHeader(HttpHeaders.Options)
//        allowHeader(HttpHeaders.Put)
        allowCredentials = true
        maxAgeInSeconds = 3600 // Cache preflight requests for 1 hour

    }
}
