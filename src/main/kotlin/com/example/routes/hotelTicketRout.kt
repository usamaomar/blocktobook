package com.example.routes

import com.example.data.repository.hotelTicketsDataSource.HotelTicketDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.hotelTicketModel.CreateHotelTicketModel
import com.example.domain.model.hotelTicketModel.UpdateHotelTicketModel
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
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 641

fun Route.hotelTicketRout() {
    val hotelTicketDataSource: HotelTicketDataSource by KoinJavaComponent.inject(
        HotelTicketDataSource::class.java
    )
        post(Api.HotelTicket.POST.path) {
            try {
                val request = call.receiveModel<CreateHotelTicketModel>()
                val authorization = call.request.headers["Authorization"]
                val decodedPayload = decodeJwtPayload(authorization ?: "")
                val userId = decodedPayload["userId"] ?: ""
                val city = hotelTicketDataSource.post(userId,request)
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
        }

        put(Api.HotelTicket.Put.path) {
            try {
                val request = call.receiveModel<UpdateHotelTicketModel>()
                val authorization = call.request.headers["Authorization"]
                val decodedPayload = decodeJwtPayload(authorization ?: "")
                val userId = decodedPayload["userId"] ?: ""
                val responseCall = hotelTicketDataSource.put(userId,request)
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

       get(Api.HotelTicket.GetAll.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = hotelTicketDataSource.getAll(
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
                filterByHotelIds = call.parameters[paramNames.FilterByHotelIds].toSafeString()?.split(",")?.map { it},
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