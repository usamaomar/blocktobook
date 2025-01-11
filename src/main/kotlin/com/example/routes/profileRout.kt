package com.example.routes

import com.example.data.repository.cityDataSource.ProfileDataSource
import com.example.data.repository.sendGrid.SendGridDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.profileModel.CompanyInfoModel
import com.example.domain.model.profileModel.CreateCompanyInfoModel
import com.example.domain.model.profileModel.UpdateCompanyInfoModel
import com.example.domain.model.profileModel.VerifiedCompanyInfoModel
import com.example.domain.model.profileModel.toCompanyInfoModel
import com.example.domain.model.profileModel.toCompanyInfoModelUser
import com.example.domain.model.subscriptionModel.Subscription
import com.example.domain.model.subscriptionModel.UpdateSubscription
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.receiveModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 9
val sendGridDataSource: SendGridDataSource by KoinJavaComponent.inject(SendGridDataSource::class.java)

fun Route.profileRout() {
    val profileDataSource: ProfileDataSource by KoinJavaComponent.inject(ProfileDataSource::class.java)
    put(Api.Profile.UpdateCompany.path) {
        try {
            val request = call.receiveModel<CompanyInfoModel>()
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val responseCall =
                profileDataSource.updateCompanyInfo(decodedPayload["userId"], request)
            call.respond(
                message = responseCall ?: ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
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

    put(Api.Profile.UpdateSubscription.path) {
        val subscription = call.receiveModel<Subscription>()
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val user = profileDataSource.updateSubscription(
                userId = decodedPayload["userId"] ?: "",
                subscription = subscription.type,
            )
            call.respond(
                message = user ?: ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
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
    put(Api.Profile.UpdateUserSubscription.path) {
        val subscription = call.receiveModel<UpdateSubscription>()
        try {
            val user = profileDataSource.updateSubscription(
                userId = subscription.userId,
                subscription = subscription.type,
            )
            call.respond(
                message = user ?: ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
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

    put(Api.Profile.UpdateUserCompany.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val request = call.receiveModel<UpdateCompanyInfoModel>()
            val responseCall =
                profileDataSource.updateCompanyInfo(request.userId, request.toCompanyInfoModel())

            if(request.isCompanyInfoVerified){
                sendGridDataSource.confiramtionOfAccountApprove(
                    request.userId,
                    decodedPayload["userId"] ?: "",
                    "تمت الموافقه على انشاء الحساب"
                )
            }else{
                sendGridDataSource.confiramtionOfAccountApprove(
                    request.userId,
                    decodedPayload["userId"] ?: "",
                    "تم تعطيل الحساب"
                )
            }

            call.respond(
                message = responseCall ?: ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
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
    put(Api.Profile.UpdateUserCompanyByUser.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val request = call.receiveModel<CreateCompanyInfoModel>()
            val responseCall =
                profileDataSource.updateUserCompanyInfo(
                    decodedPayload["userId"] ?: "",
                    request.toCompanyInfoModelUser(
                        blockToBookFees = 10.0,
                        isCompanyInfoVerified = false
                    )
                )
            val formattedMessage = """
    قام بإنشاء حساب جديد:
    اسم الشركة: ${request.name}
    رقم الهاتف: ${request.phoneNumber}
    رقم المنشأة: ${request.facilityNumber ?: "غير متوفر"}
""".trimIndent()
            sendGridDataSource.sendEmailToAllAdminsUsingSendGrid(
                decodedPayload["userId"] ?: "",
                formattedMessage
            )
            call.respond(
                message = responseCall ?: ApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
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

    put(Api.Profile.AdminUserApprove.path) {
        try {
            val request = call.receiveModel<VerifiedCompanyInfoModel>()
            val responseCall =
                profileDataSource.adminUserApprove(request)
            call.respond(
                message = responseCall
            )
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