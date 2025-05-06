package com.example.routes

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.userModel.UserSession
import com.example.data.repository.userDataSource.UserDataSource
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.userModel.UpdateUser
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.paramNames
import com.example.util.receiveModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 10

fun Route.userRoute() {
    val userDataSource: UserDataSource by KoinJavaComponent.inject(UserDataSource::class.java)
    get(Api.User.GetUsingId.path) {
        try {
            val user = userDataSource.getUserInfo(call.parameters[paramNames.Id] ?: "")
            call.respond(
                message = ApiResponse(
                    succeeded = true,
                    data = user,
                    errorCode = errorCode
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
    get(Api.User.IsTest.path) {
        try {
            call.respond(
                message = ApiResponse(
                    succeeded = true,
                    data = null,
                    errorCode = errorCode
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
    get(Api.User.GetById.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val user = userDataSource.getUserInfo(decodedPayload["userId"] ?: "")
            call.respond(
                message = ApiResponse(
                    succeeded = true,
                    data = user,
                    errorCode = errorCode
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
    get(Api.User.GetImageArray.path) {
        try {
            val user = userDataSource.getImageArray(call.parameters[paramNames.ImageUrl] ?: "")
            call.respond(
                message = ApiResponse(
                    succeeded = true,
                    data = user,
                    errorCode = errorCode
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
    put(Api.User.Put.path) {
        try {
            val userUpdate = call.receiveModel<UpdateUser>()
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val user = userDataSource.updateUserInfo(
                userId = userId,
                userUpdate
            )
            call.respond(
                message = ApiResponse(
                    succeeded = true,
                    data = user,
                    message = arrayListOf("Successfully Updated"), errorCode = errorCode
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
    get(Api.User.GetAll.path) {
        try {
            val xurrenttime: Long = System.currentTimeMillis()
            val pagingApiResponse = userDataSource.getAll(
                searchText = call.parameters[paramNames.SearchText] ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                xurrenttime = xurrenttime,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toInt()
                    ?: 1,
            )
            call.respond(
                message = pagingApiResponse ?: PagingApiResponse(
                    succeeded = false,
                    message = arrayListOf("Something went wrong"),
                    data = null, errorCode = errorCode
                )
            )
        } catch (e: Exception) {
            call.respond(
                message = ApiResponse(
                    succeeded = false,
                    message = arrayListOf(e.message.toString(),e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
}