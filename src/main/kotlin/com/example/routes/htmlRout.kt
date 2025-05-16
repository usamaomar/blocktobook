package com.example.routes

import com.example.endPoints.Api
import io.ktor.server.application.call
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.html.*
import io.ktor.server.http.content.*
import io.ktor.server.response.respondFile
import java.io.File


fun Route.htmlRout() {
    get(Api.Web.Pyment.path) {
        // Extract parameters from the request
//        val checkoutId = call.parameters["checkoutId"] ?: ""
//        val integrity = call.parameters["integrity"] ?: ""
//        val isLive = call.parameters["isLive"]?.toBoolean() ?: false
//
//        // Log the received parameters (for debugging)
//        println("Received request for checkout with ID: $checkoutId")
//        println("Integrity: $integrity")
//        println("Is Live: $isLive")

        // Serve the checkout.html file
        // The parameters are passed in the URL and extracted by the JavaScript in the HTML
        call.respondFile(File("src/main/resources/static/checkout.html"))
    }

    // Serve static files from resources/static directory
    static("/") {
        resources("static")
    }

}