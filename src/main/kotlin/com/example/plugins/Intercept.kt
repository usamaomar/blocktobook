package com.example.plugins

import com.example.data.repository.userDataSource.UserDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.endPoints.Api
import com.example.util.AccessRole
import com.example.util.Constants.COMPANY_INFO_NOT_FOUND
import com.example.util.Constants.COMPANY_INFO_NOT_VERIFIED
import com.example.util.Constants.SUBSCRIPTION_NOT_FOUND
import com.example.util.Constants.TOKEN_IS_EXPIRED
import com.example.util.Constants.YOU_DONT_HAVE_ACCESS
import com.example.util.removeQueryParams
import com.example.util.toEnum
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.java.KoinJavaComponent
import java.util.Base64
import java.util.Date

fun Application.intercept() {
    val userDataSource: UserDataSource by KoinJavaComponent.inject(UserDataSource::class.java)
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(mapOf("error" to cause.localizedMessage).toString()),
                    data = null
                ), status = HttpStatusCode.InternalServerError
            )
        }
//        status(HttpStatusCode.NotFound) { call, value ->
//            call.respond(
//                message = ApiResponse(
//                    succeeded = false,
//                    message = arrayListOf(value.description),
//                    data = null
//                ), status = HttpStatusCode.InternalServerError
//            )
//        }
        status(HttpStatusCode.MethodNotAllowed) { call, value ->
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(value.description),
                    data = null
                ), status = HttpStatusCode.InternalServerError
            )
        }
        status(HttpStatusCode.InternalServerError) { call, value ->
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(value.description),
                    data = null
                ), status = HttpStatusCode.InternalServerError
            )
        }
    }

    intercept(ApplicationCallPipeline.Plugins) {
        if (!excludedPaths.contains(call.request.path().removeQueryParams())) {
            if(containsUploadsPath("http://localhost:8080/app/uploads/1727803989932093.png")){
                return@intercept
            }
            val authorization = call.request.headers["Authorization"]

            if (authorization == null) {
                call.respondUnauthorized("Not authorized")
                return@intercept
            }

            try {
                val decodedPayload = decodeJwtPayload(authorization)

                // Check if the JWT token is expired
                if (isJwtExpired(decodedPayload)) {
                    call.respondUnauthorized("Token is expired", TOKEN_IS_EXPIRED)
                    return@intercept
                }

                val userModel = userDataSource.getUserInfo(decodedPayload["userId"] ?: "")
                if (userModel == null) {
                    call.respondUnauthorized("User Not Found")
                    return@intercept
                }

                if (userModel.accessRole == AccessRole.User.ordinal.toEnum<AccessRole>() && notAllowedUserPaths.contains(call.request.path().removeQueryParams())) {
                        call.respondUnauthorized("You don't have access", YOU_DONT_HAVE_ACCESS)
                        return@intercept
                }

                if (userModel.accessRole == AccessRole.Merchant.ordinal.toEnum<AccessRole>() && notAllowedMerchantPaths.contains(call.request.path().removeQueryParams())) {
                    call.respondUnauthorized("You don't have access", YOU_DONT_HAVE_ACCESS)
                    return@intercept
                }

                if (userModel.accessRole == AccessRole.Merchant.ordinal.toEnum<AccessRole>() && notAllowedMerchantPaths.contains(call.request.path().removeQueryParams())) {
                    call.respondUnauthorized("You don't have access", YOU_DONT_HAVE_ACCESS)
                        return@intercept
                }
                if (userModel.accessRole == AccessRole.Merchant.ordinal.toEnum<AccessRole>() && !allowedMerchantPaths.contains(call.request.path().removeQueryParams())) {

                    // Ensure company info is present and verified
                    if (userModel.companyInfo == null) {
                        call.respondUnauthorized("Company Info Not Found", COMPANY_INFO_NOT_FOUND)
                        return@intercept
                    }

                    if (!userModel.companyInfo.isCompanyInfoVerified) {
                        call.respondUnauthorized("Company Info Not Verified", COMPANY_INFO_NOT_VERIFIED)
                        return@intercept
                    }

                    // Ensure subscription is valid and not expired
                    if (userModel.subscription == null) {
                        call.respondUnauthorized("Subscription Not Found", SUBSCRIPTION_NOT_FOUND)
                        return@intercept
                    }

//
//                    if (userModel.subscription.expirationDate!! < System.currentTimeMillis()) {
//                        call.respondUnauthorized("Subscription Expired", SUBSCRIPTION_EXPIRED)
//                        return@intercept
//                    }
                }



            } catch (e: Exception) {
                call.respondBadRequest("Invalid JWT: ${e.localizedMessage}")
            }
        }
    }
}

fun containsUploadsPath(url: String): Boolean {
    return url.contains("app/uploads/")
}

fun isJwtExpired(payload: Map<String, String>): Boolean {
    val expClaim = payload["exp"] ?: return true
    val expTime = expClaim.toLongOrNull() ?: return true
    val currentTime = Date().time / 1000  // Convert to seconds
    return expTime < currentTime
}

val excludedPaths = listOf(
    Api.Auth.GoogleLoginMerchant.path,
    Api.Auth.LoginByEmailMerchant.path,
    Api.Auth.CreateEmailMerchant.path,
    Api.Auth.CreateEmailAdmin.path,
    Api.Auth.ForgotPassword.path,
    Api.Auth.Refresh.path,
    Api.SubscriptionTypes.GetAll.path,
)
val allowedMerchantPaths = listOf(
    Api.Profile.UpdateCompany.path,
    Api.Profile.UpdateUserCompanyByUser.path,
    Api.Profile.UpdateSubscription.path,
    Api.Payment.CreateSubscriptionCheckout.path,
    Api.Payment.GetSubscriptionPaymentStatus.path,
    Api.Upload.Create.path,
)

val notAllowedMerchantPaths = listOf(
    Api.User.GetAll.path
)
val notAllowedUserPaths = listOf(
    Api.User.GetAll.path
)
suspend fun ApplicationCall.respondUnauthorized(message: String, code: Int? = null) {
    respond(
        ApiResponse(
            succeeded = false,
            message = arrayListOf(message),
            data = null,
            code = code
        )
    )
}

suspend fun ApplicationCall.respondBadRequest(message: String) {
    respond(
        ApiResponse(
            succeeded = false,
            message = arrayListOf(message),
            data = null
        )
    )
}



fun decodeJwtPayload(jwt: String): Map<String, String> {
    val parts = jwt.split(".")
    if (parts.size != 3) throw IllegalArgumentException("Invalid JWT format")
    val payload = String(Base64.getUrlDecoder().decode(parts[1]))
    val jsonPayload = Json.parseToJsonElement(payload).jsonObject
    return jsonPayload.mapValues { it.value.jsonPrimitive.content }
}