package com.example.data.repository.userDataSource

import com.example.domain.model.publicModel.ApiResponse
import io.ktor.http.content.MultiPartData


interface UploadDataSource {
    suspend fun create(multiPart: MultiPartData): ApiResponse<String?>

}