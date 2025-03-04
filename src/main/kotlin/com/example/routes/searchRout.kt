package com.example.routes

import com.example.data.repository.searchDataSource.SearchDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.paramNames
import com.example.util.toSafeBoolean
import com.example.util.toSafeDouble
import com.example.util.toSafeInt
import com.example.util.toSafeLong
import com.example.util.toSafeString
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 9

fun Route.searchRout() {
    val searchDataSource: SearchDataSource by KoinJavaComponent.inject(SearchDataSource::class.java)
    get(Api.Search.GetAllByCityNameAndHotelName.path) {
        try {
            val pagingApiResponse = searchDataSource.getAllByCityNameAndHotelName(
                searchText = call.parameters[paramNames.SearchText]?.toSafeString() ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toSafeInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toSafeInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
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
    get(Api.Search.GetAllByCityNameAndAirportsName.path) {
        try {
            val pagingApiResponse = searchDataSource.getAllByCityNameAndAirportsName(
                searchText = call.parameters[paramNames.SearchText]?.toSafeString() ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toSafeInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toSafeInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
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
    get(Api.Search.GetAllTicketsFiltration.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = searchDataSource.getAllTicketsFiltration(
                userId = userId,
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
                filterByDateFrom = call.parameters[paramNames.FilterByDateFrom]?.toSafeLong(),
                filterByDateTo= call.parameters[paramNames.FilterByDateTo]?.toSafeLong(),
                filterByHotelId = call.parameters[paramNames.FilterByHotelId]?.toSafeString(),
                filterByCityId = call.parameters[paramNames.FilterByCityId]?.toSafeString(),
                filterByAdultsTicketNumber = call.parameters[paramNames.FilterByAdultsTicketNumber]?.toSafeInt() ?: 1,
                filterByRoomsTicketNumber = call.parameters[paramNames.FilterByRoomsTicketNumber]?.toSafeInt() ?: 1,
                filterByChildrenTicketNumber = call.parameters[paramNames.FilterByChildrenTicketNumber]?.toSafeInt(),
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


    get(Api.Search.GetAllFlightTicketsFiltration.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = searchDataSource.getAllFlightTicketsFiltration(
                userId = userId,
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
                filterByDateFrom = call.parameters[paramNames.FilterByDateFrom]?.toSafeLong(),
                filterByDateTo= call.parameters[paramNames.FilterByDateTo]?.toSafeLong(),
                filterByIdFromAirport = call.parameters[paramNames.FilterByIdFromAirport]?.toSafeString(),
                filterByIdFromCity = call.parameters[paramNames.FilterByIdFromCity]?.toSafeString(),
                filterByIdToAirport = call.parameters[paramNames.FilterByIdToAirport]?.toSafeString(),
                filterByIdToCity = call.parameters[paramNames.FilterByIdToCity]?.toSafeString(),
                filterByAdultsTicketNumber = call.parameters[paramNames.FilterByAdultsTicketNumber]?.toSafeInt() ?: 1,
                filterByChildrenTicketNumber = call.parameters[paramNames.FilterByChildrenTicketNumber]?.toSafeInt(),
                directFlightOnly = call.parameters[paramNames.DirectFlightOnly]?.toSafeBoolean(),
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
    get(Api.Search.GetAllMonthTicketsFiltration.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = searchDataSource.getAllMonthTicketsFiltration(
                userId = userId,
                filterByDate = call.parameters[paramNames.FilterByDate]?.toSafeLong(),
                filterByHotelId = call.parameters[paramNames.FilterByHotelId]?.toSafeString(),
                filterByCityId = call.parameters[paramNames.FilterByCityId]?.toSafeString(),
                filterByAdultsTicketNumber = call.parameters[paramNames.FilterByAdultsTicketNumber]?.toSafeInt() ?: 1,
                filterByRoomsTicketNumber = call.parameters[paramNames.FilterByRoomsTicketNumber]?.toSafeInt() ?: 1,
                filterByChildrenTicketNumber = call.parameters[paramNames.FilterByChildrenTicketNumber]?.toSafeInt(),
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


    get(Api.Search.GetAllMonthFlightTicketsFiltration.path) {
        try {
            val authorization = call.request.headers["Authorization"]
            val decodedPayload = decodeJwtPayload(authorization ?: "")
            val userId = decodedPayload["userId"] ?: ""
            val pagingApiResponse = searchDataSource.getAllMonthFlightTicketsFiltration(
                userId = userId,
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt() ?: 1,
                filterByDate = call.parameters[paramNames.FilterByDate]?.toSafeLong(),
                filterByIdFromAirport = call.parameters[paramNames.FilterByIdFromAirport]?.toSafeString(),
                filterByIdFromCity = call.parameters[paramNames.FilterByIdFromCity]?.toSafeString(),
                filterByIdToAirport = call.parameters[paramNames.FilterByIdToAirport]?.toSafeString(),
                filterByIdToCity = call.parameters[paramNames.FilterByIdToCity]?.toSafeString(),
                filterByAdultsTicketNumber = call.parameters[paramNames.FilterByAdultsTicketNumber]?.toSafeInt() ?: 1,
                filterByChildrenTicketNumber = call.parameters[paramNames.FilterByChildrenTicketNumber]?.toSafeInt(),
                directFlightOnly = call.parameters[paramNames.DirectFlightOnly]?.toSafeBoolean(),
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


    get(Api.Search.GetReturnTicketDate.path) {
        try {
            val pagingApiResponse = searchDataSource.getReturnTicketDate(
                returnTicketId = call.parameters[paramNames.Id].toSafeString() ?: ""
            )
            call.respond(
                message = pagingApiResponse ?:  ApiResponse(
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