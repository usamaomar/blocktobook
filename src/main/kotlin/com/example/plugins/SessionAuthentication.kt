//package com.example.plugins
//
//import com.example.domain.model.publicModel.ApiResponse
//import com.example.domain.model.userModel.UserSession
//import io.ktor.server.application.Application
//import io.ktor.server.application.install
//import io.ktor.server.auth.Authentication
//import io.ktor.server.auth.session
//import io.ktor.server.response.respond
//
//fun Application.sessionAuthentication() {
//    install(Authentication){
//        session<UserSession>(name = "auth-session") {
//            validate { session -> session }
//            challenge {
//                call.respond(
//                    message = ApiResponse(
//                        succeeded = false,
//                        message = arrayListOf("Invalid session"),
//                        data = null
//                    ), status = io.ktor.http.HttpStatusCode.ExpectationFailed
//                )
//            }
//        }
//    }
//}