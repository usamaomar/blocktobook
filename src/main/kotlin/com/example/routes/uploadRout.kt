package com.example.routes

import com.example.data.repository.userDataSource.UploadDataSource
import com.example.domain.model.publicModel.ApiResponse
import com.example.endPoints.Api
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.java.KoinJavaComponent

private const val errorCode: Int = 18

fun Route.uploadRoute(

) {
    val uploadDataSource: UploadDataSource by KoinJavaComponent.inject(UploadDataSource::class.java)
        post(Api.Upload.Create.path) {
            try {
                val multipart = call.receiveMultipart()
                val user = uploadDataSource.create(multipart)
                call.respond(
                    message = user ?: ApiResponse(
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