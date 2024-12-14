package com.example.routes

import com.example.data.repository.SubscriptionTypesDataSource.SubscriptionTypesDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.subscriptionTypesModel.CreateSubscriptionTypeModel
import com.example.domain.model.subscriptionTypesModel.UpdateSubscriptionTypeModel
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.paramNames
import com.example.util.receiveModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 9

fun Route.subscriptionTypeRout() {
    val subscriptionTypeModel: SubscriptionTypesDataSource by KoinJavaComponent.inject(
        SubscriptionTypesDataSource::class.java
    )
        get(Api.SubscriptionTypes.GetById.path) {
            try {
                try {
                    val pagingApiResponse = subscriptionTypeModel.getById(
                        id = call.parameters[paramNames.Id] ?: ""
                    )
                    call.respond(
                        message = pagingApiResponse ?: ApiResponse(
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
        get(Api.SubscriptionTypes.GetAll.path) {
            try {
                try {
                    val authorization = call.request.headers["Authorization"]
                    val decodedPayload = decodeJwtPayload(authorization ?: "")
                    val userId = decodedPayload["userId"] ?: ""
                    val pagingApiResponse = subscriptionTypeModel.getAll(
                        userId = userId,
                        searchText = call.parameters[paramNames.SearchText] ?: "",
                        pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
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
        post(Api.SubscriptionTypes.POST.path) {
            try {
                 val request = call.receiveModel<CreateSubscriptionTypeModel>()
                try {
                    val city = subscriptionTypeModel.post(request)
                    call.respond(
                        message = city?: ApiResponse(
                            succeeded = false,
                            message = arrayListOf("Something went wrong"),
                            data = null, errorCode = errorCode
                        )
                    )
                } catch (e: Exception) {
                    call.respond(
                        message = ApiResponse(
                            succeeded = false,
                            message = arrayListOf(
                                e.message.toString(),
                                e.cause?.message.toString()
                            ),
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
        put(Api.SubscriptionTypes.PUT.path) {
            try {
                val request = call.receiveModel<UpdateSubscriptionTypeModel>()
                val responseCall = subscriptionTypeModel.put(request)
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
                        message = arrayListOf(e.message.toString(),e.cause?.message.toString()),
                        data = null, errorCode = errorCode
                    ), status = HttpStatusCode.ExpectationFailed
                )
            }
        }
}