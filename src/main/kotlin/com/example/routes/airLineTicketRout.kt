package com.example.routes

import com.example.data.repository.airLinesTicketsDataSource.AirLineTicketDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.userModel.UserSession
import com.example.domain.model.airlinesTicketModel.CreateAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.UpdateAirlineTicketModel
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.paramNames
import com.example.util.receiveModel
import com.example.util.toSafeBoolean
import com.example.util.toSafeDouble
import com.example.util.toSafeInt
import com.example.util.toSafeLong
import com.example.util.toSafeString
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 645

fun Route.airLineTicketRout() {
    val airLineTicketDataSource: AirLineTicketDataSource by KoinJavaComponent.inject(
        AirLineTicketDataSource::class.java
    )
        post(Api.AirLineTicket.POST.path) {
            try {
                try {
                    val request = call.receiveModel<CreateAirlineTicketModel>()
                    val authorization = call.request.headers["Authorization"]
                    val decodedPayload = decodeJwtPayload(authorization ?: "")
                    val userId = decodedPayload["userId"] ?: ""
                    val city = airLineTicketDataSource.post(userId,request)
                    call.respond(
                        message = city ?: ApiResponse(
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

        put(Api.AirLineTicket.Put.path) {
            try {
                val request = call.receiveModel<UpdateAirlineTicketModel>()
                val authorization = call.request.headers["Authorization"]
                val decodedPayload = decodeJwtPayload(authorization ?: "")
                val userId = decodedPayload["userId"] ?: ""
                val responseCall = airLineTicketDataSource.put(userId,request)
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

        get(Api.AirLineTicket.GetById.path) {
            try {
                val userSession = call.principal<UserSession>()
                if (userSession == null) {
                    call.respond(
                        message = ApiResponse(
                            succeeded = false,
                            message = arrayListOf("Invalid session"),
                            data = null, errorCode = errorCode
                        ),
                        status = HttpStatusCode.ExpectationFailed,)
                } else {
                    try {
                        val pagingApiResponse = airLineTicketDataSource.getById(
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
        get(Api.AirLineTicket.GetAll.path) {
            try {
                val authorization = call.request.headers["Authorization"]
                val decodedPayload = decodeJwtPayload(authorization ?: "")
                val userId = decodedPayload["userId"] ?: ""
                val pagingApiResponse = airLineTicketDataSource.getAll(
                    userId = userId,
                    searchText = call.parameters[paramNames.SearchText] ?: "",
                    pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                    pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                    xAppLanguageId = call.parameters[paramNames.languageId]?.toSafeInt() ?: 1,
                    filterByDateFrom = call.parameters[paramNames.FilterByDateFrom]?.toSafeLong(),
                    filterByDateTo= call.parameters[paramNames.FilterByDateTo]?.toSafeLong(),
                    filterByVisibility = call.parameters[paramNames.FilterByVisibility]?.let { it.toSafeBoolean() },
                    filterByPriceRangeFrom = call.parameters[paramNames.FilterByPriceRangeFrom]?.toSafeDouble(),
                    filterByPriceRangeTo = call.parameters[paramNames.FilterByPriceRangeTo]?.toSafeDouble(),
                    filterByAirLineIds = call.parameters[paramNames.FilterByAirLineIds].toSafeString()?.split(",")?.map { it},
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


        get(Api.AirLineTicket.GetAirlins.path) {
            try {
                val authorization = call.request.headers["Authorization"]
                val decodedPayload = decodeJwtPayload(authorization ?: "")
                val userId = decodedPayload["userId"] ?: ""
                val pagingApiResponse = airLineTicketDataSource.getAirlineTicketDetailsById(
                    ticketId = call.parameters[paramNames.Id]?.toSafeString() ?: "",
                    xAppLanguageId = call.parameters[paramNames.languageId]?.toSafeInt() ?: 1,
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


    get(Api.AirLineTicket.GetNumbers.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = airLineTicketDataSource.getNumberOfRemainingSeatsById(
                ticketId = call.parameters[paramNames.Id]?.toSafeString() ?: "",
                userId = userId,
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