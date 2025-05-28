package com.example.data.repository.visaDataSource

import com.example.domain.model.publicModel.ApiResponse
import io.ktor.http.content.MultiPartData


interface VisaDataSource {
    suspend fun create(multiPart: MultiPartData): ApiResponse<String?>

}