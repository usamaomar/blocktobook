package com.example.routes

import com.example.data.repository.hotelDataSource.HotelDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.userModel.UserSession
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.hotelModel.CreateHotelModel
import com.example.domain.model.hotelModel.UpdateHotelModel
import com.example.endPoints.Api
import com.example.util.paramNames
import com.example.util.receiveModel
import com.example.util.toSafeInt
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 6

fun Route.hotelRout() {
    val hotelDataSource: HotelDataSource by KoinJavaComponent.inject(HotelDataSource::class.java)
        get(Api.Hotels.GetById.path) {
            try {
                val pagingApiResponse = hotelDataSource.getById(
                    id = call.parameters[paramNames.Id] ?: "",
                    xAppLanguageId = call.request.headers[paramNames.languageId]?.toInt()
                        ?: 1,
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
        get(Api.Hotels.GetAll.path) {
            try {
                val pagingApiResponse = hotelDataSource.getAll(
                    searchText = call.parameters[paramNames.SearchText] ?: "",
                    pageSize = call.parameters[paramNames.PageSize]?.toSafeInt() ?: 0,
                    pageNumber = call.parameters[paramNames.PageNumber]?.toSafeInt() ?: 0,
                    filterByCityId = call.parameters[paramNames.FilterByCityId] ?: "",
                    xAppLanguageId = call.request.headers[paramNames.languageId]?.toSafeInt()
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
        post(Api.Hotels.POST.path) {
            try {
                 val request = call.receiveModel<CreateHotelModel>()
                val city = hotelDataSource.post(request)
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
        put(Api.Hotels.PUT.path) {
            try {
                val request = call.receiveModel<UpdateHotelModel>()
                val responseCall = hotelDataSource.put(request)
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