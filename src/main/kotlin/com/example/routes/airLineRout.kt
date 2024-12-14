package com.example.routes

import com.example.data.repository.airLinesDataSource.AirLineDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.userModel.UserSession
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.airlinesModel.CreateAirLine
import com.example.domain.model.airlinesModel.UpdateAirLine
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

fun Route.airLineRout(

) {
    val airLineDataSource: AirLineDataSource by KoinJavaComponent.inject(AirLineDataSource::class.java)
        get(Api.AirLines.GetById.path) {
            try {
                val pagingApiResponse = airLineDataSource.getById(
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
        get(Api.AirLines.GetAll.path) {
            try {
                val pagingApiResponse = airLineDataSource.getAll(
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
        }
        post(Api.AirLines.POST.path) {
            try {
                val request = call.receiveModel<CreateAirLine>()
                val city = airLineDataSource.post(request)
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
        put(Api.AirLines.PUT.path) {
            try {
                val request = call.receiveModel<UpdateAirLine>()
                val responseCall = airLineDataSource.put(request)
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