package com.example.routes

import com.example.data.repository.userDataSource.UploadDataSource
import com.example.domain.model.airlinesTicketModel.CreateAirlineTicketModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.endPoints.Api
import com.example.plugins.decodeJwtPayload
import com.example.util.receiveModel
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 18

fun Route.visaRoute(

) {
    val uploadDataSource: UploadDataSource by KoinJavaComponent.inject(UploadDataSource::class.java)
        post(Api.Visa.POST.path) {
            try {
                val request = call.receiveModel<CreateAirlineTicketModel>()
                val authorization = call.request.headers["Authorization"]
                val decodedPayload = decodeJwtPayload(authorization ?: "")
                val userId = decodedPayload["userId"] ?: ""
                call.respond(
                    message = user
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