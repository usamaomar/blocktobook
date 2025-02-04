package com.example.routes

import com.example.data.repository.cityDataSource.CityDataSource
import com.example.data.repository.sendGrid.SendGridDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.cityModel.CreateCityModel
import com.example.domain.model.cityModel.CreateSendGrid
import com.example.domain.model.cityModel.UpdateCityModel
import com.example.endPoints.Api
import com.example.util.paramNames
import com.example.util.paramNames.FilterByCityId
import com.example.util.receiveModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 9

fun Route.cityRout() {
    val cityDataSource: CityDataSource by KoinJavaComponent.inject(CityDataSource::class.java)
    val sendGridDataSource: SendGridDataSource by KoinJavaComponent.inject(SendGridDataSource::class.java)
        get(Api.Cities.GetById.path) {
            try {
                val pagingApiResponse = cityDataSource.getById(
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
        get(Api.Cities.GetAll.path) {
            try {
                val pagingApiResponse = cityDataSource.getAll(
                    searchText = call.parameters[paramNames.SearchText] ?: "",
                    pageSize = call.parameters[paramNames.PageSize]?.toInt() ?: 0,
                    pageNumber = call.parameters[paramNames.PageNumber]?.toInt() ?: 0,
                    xAppLanguageId = call.request.headers[paramNames.languageId]?.toInt() ?: 1,
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
        post(Api.Cities.POST.path) {
            try {
                val request = call.receiveModel<CreateCityModel>()
                val city = cityDataSource.post(request,call.request.headers[paramNames.languageId]?.toInt() ?: 1)
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
    post(Api.Cities.sendTestSendGrid.path) {
            try {
                val request = call.receiveModel<CreateSendGrid>()
                val city = sendGridDataSource.sendTestSendGrid(request.fromEmail,request.toEmail,request.text)
                call.respond(
                    message = city
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
        put(Api.Cities.PUT.path) {
            try {
                val request = call.receiveModel<UpdateCityModel>()
                val responseCall = cityDataSource.put(request,call.request.headers[paramNames.languageId]?.toInt() ?: 1)
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