package com.example.routes

import com.example.data.repository.cartDataSource.CartDataSource
import com.example.domain.model.cartModel.CreateCartModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.paramNames
import com.example.util.receiveModel
import com.example.util.toSafeInt
import com.example.util.toSafeString
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 990

fun Route.cartRout() {
    val cartDataSource: CartDataSource by KoinJavaComponent.inject(CartDataSource::class.java)
        post(Api.Cart.POST.path) {
            try {
                val authorization = call.request.headers["Authorization"]
                val decodedPayload = decodeJwtPayload(authorization ?: "")
                val userId = decodedPayload["userId"] ?: ""
                val request = call.receiveModel<CreateCartModel>()
                val createCart = cartDataSource.post(userId,request,call.request.headers[paramNames.languageId]?.toInt() ?: 1)
                call.respond(
                    message = createCart ?: ApiResponse(
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

    get(Api.Cart.GetAll.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = cartDataSource.getAll(
                userId = userId,
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1
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

    get(Api.Cart.GetCartAmount.path) {
        try {
            val pagingApiResponse = cartDataSource.getAmount(
                userId = call.parameters[paramNames.Id]?.toSafeString() ?: "",
            )
            call.respond(
                message = pagingApiResponse
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

    delete(Api.Cart.DELETE.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val ticketId = call.parameters[paramNames.Id]?.toSafeString() ?: ""
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val createCart = cartDataSource.delete(userId,ticketId)
            call.respond(
                message = createCart ?: ApiResponse(
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