package com.example.routes

import com.example.data.repository.airPortsDataSource.AirPortDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.userModel.UserSession
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.airportsModel.CreateAirPort
import com.example.domain.model.airportsModel.UpdateAirPort
import com.example.endPoints.Api
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
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 6

fun Route.airPortRout(

) {
    val airPortDataSource: AirPortDataSource by KoinJavaComponent.inject(AirPortDataSource::class.java)
    get(Api.AirPorts.GetById.path) {
        try {
            val pagingApiResponse = airPortDataSource.getById(
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
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    get(Api.AirPorts.GetAll.path) {
        try {
            val pagingApiResponse = airPortDataSource.getAll(
                searchText = call.parameters[paramNames.SearchText] ?: "",
                pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                xAppLanguageId = call.request.headers[paramNames.languageId]?.toInt()
                    ?: 1,
                filterByCityId = call.parameters[paramNames.FilterByCityId] ?: "",
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
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    post(Api.AirPorts.POST.path) {
        try {
            val request = call.receiveModel<CreateAirPort>()
            val city = airPortDataSource.post(request)
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
                    message = arrayListOf(e.message.toString(), e.cause?.message.toString()),
                    data = null, errorCode = errorCode
                ), status = HttpStatusCode.ExpectationFailed
            )
        }
    }
    put(Api.AirPorts.PUT.path) {
        try {
            val request = call.receiveModel<UpdateAirPort>()
            val responseCall = airPortDataSource.put(request)
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
}