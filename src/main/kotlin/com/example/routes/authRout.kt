package com.example.routes

import com.example.data.repository.authDataSource.AuthDataSource
import com.example.domain.model.authModel.CreateEmailAdminModel
import com.example.domain.model.authModel.CreateEmailModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.authModel.CreateRefreshTokenModel
import com.example.domain.model.authModel.CreateTokenModel
import com.example.domain.model.authModel.FirebaseForgetPasswordRequest
import com.example.domain.model.authModel.ForgetPasswordEmailModel
import com.example.domain.model.authModel.ResponseTokenModel
import com.example.endPoints.Api
import com.example.util.receiveModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.json.Json
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 8

fun Route.authRout() {
    val authDataSource: AuthDataSource by KoinJavaComponent.inject(AuthDataSource::class.java)
    post(Api.Auth.GoogleLoginMerchant.path) {
        try {
            val request = call.receive<CreateTokenModel>()
            if (request.tokenId?.isNotEmpty() == true) {

               FirebaseAuth.getInstance().verifyIdToken(request.tokenId).run {
                    this.email
                   println()
                }
                val mapModel = authDataSource.googleLoginMerchant(request.tokenId)
//                call.sessions.set(mapModel?.get("UserSession") as UserSession)
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("e.message.toString(), e.cause?.message.toString()"),
                        data = null, errorCode = errorCode
                    )
                )
            }
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    post(Api.Auth.LoginByEmailMerchant.path) {
        try {
            val request = call.receiveModel<CreateTokenModel>()
            FirebaseAuth.getInstance().verifyIdToken(request.tokenId).run {
                if(this!=null){
                    val mapModel = authDataSource.loginByToken(CreateEmailModel(this.email, this.name ?: "user", this.uid))
                    call.respond(
                        message = mapModel?.get("ApiResponse") as ApiResponse<*>
                    )
                }else{
                    call.respond(
                        message = ApiResponse(
                            succeeded = false,
                            message = arrayListOf("Not authorized"),
                            data = null, errorCode = errorCode
                        ), status = HttpStatusCode.ExpectationFailed
                    )
                }
            }
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    post(Api.Auth.CreateEmailMerchant.path) {
        try {
            val request = call.receiveModel<CreateTokenModel>()
            FirebaseAuth.getInstance().verifyIdToken(request.tokenId).run {
                if(this!=null){
                    val mapModel = authDataSource.loginByToken(CreateEmailModel(this.email, this.name, this.uid))
                    call.respond(
                        message = mapModel?.get("ApiResponse") as ApiResponse<*>
                    )
                }else{
                    call.respond(
                        message = ApiResponse(
                            succeeded = false,
                            message = arrayListOf("Not authorized"),
                            data = null, errorCode = errorCode
                        ), status = HttpStatusCode.ExpectationFailed
                    )
                }
            }
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }

//
//            val mapModel = authDataSource.createEmailMerchant(request)
//            if (mapModel?.get("UserSession") == null) {
//                call.respond(
//                    message = ApiResponse(
//                        succeeded = false,
//                        message = arrayListOf("Account already exists"),
//                        data = null, errorCode = errorCode
//                    )
//                )
//            } else {
////                call.sessions.set(mapModel.get("UserSession") as UserSession)
//                call.respond(
//                    message = mapModel["ApiResponse"] as ApiResponse<ResponseTokenModel>
//                )
//            }
//        } catch (e: Exception) {
//            call.respond(
//                message = ApiResponse(
//                    succeeded = false,
//                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
//                    data = null, errorCode = errorCode
//                ), status = HttpStatusCode.ExpectationFailed
//            )
//        }
    }
    post(Api.Auth.CreateEmailAdmin.path) {
        try {
            val request = call.receive<CreateEmailAdminModel>()
            val mapModel = authDataSource.createEmailAdmin(request)
            if (mapModel?.get("UserSession") == null) {
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("Account already exists"),
                        data = null, errorCode = errorCode
                    )
                )
            } else {
                call.respond(
                    message = mapModel["ApiResponse"] as ApiResponse<*>
                )
            }
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    post(Api.Auth.ForgotPassword.path) {
        try {
            val request = call.receive<ForgetPasswordEmailModel>()
            val apiKey = "AIzaSyBQSf1Ikzj5Wcjv1U_6bRFHDxoSKXgQPRc"
            val firebaseClient = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            val response: HttpResponse =
                firebaseClient.post("https://identitytoolkit.googleapis.com/v1/accounts:sendOobCode?key=$apiKey") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        FirebaseForgetPasswordRequest(
                            "PASSWORD_RESET",
                            request.email
                        )
                    )
                }
            if (response.status.value == 200) {
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("Code is Sent to email"),
                        data = null, errorCode = errorCode
                    ), status = HttpStatusCode.ExpectationFailed
                )
            } else {
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("Not authorized"),
                        data = null, errorCode = errorCode
                    ), status = HttpStatusCode.ExpectationFailed
                )
            }
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    post(Api.Auth.Refresh.path) {
        try {
            val request = call.receive<CreateRefreshTokenModel>()
            if (request.token?.isNotEmpty() == true && request.refreshToken?.isNotEmpty() == true) {
                val mapModel = authDataSource.refresh(request)
                if (mapModel?.get("UserSession") == null) {
                    call.respond(
                        message = ApiResponse(
                            succeeded = false,
                            message = arrayListOf("token expired"),
                            data = null, errorCode = errorCode
                        )
                    )
                }
//                call.sessions.set(mapModel?.get("UserSession") as UserSession)
                call.respond(
                    message = mapModel?.get("ApiResponse") as ApiResponse<*>
                )
            } else {
                call.respond(
                    message = ApiResponse(
                        succeeded = false,
                        message = arrayListOf("token or refresh is empty"),
                        data = null, errorCode = errorCode
                    )
                )
            }
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
}

