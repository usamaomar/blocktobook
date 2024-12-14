package com.example.data.repository.airPortsDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.airportsModel.AirPortModel
import com.example.domain.model.airportsModel.CreateAirPort
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.airportsModel.UpdateAirPort

interface AirPortDataSource {
    suspend fun getById(id: String, xAppLanguageId: Int): ApiResponse<ResponseAirPortModel?>?
    suspend fun post(airPortModel: CreateAirPort): ApiResponse<AirPortModel?>?
    suspend fun put(updateAirPortModel: UpdateAirPort): ApiResponse<AirPortModel?>?
    suspend fun getAll(
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int, filterByCityId: String?
    ): PagingApiResponse<List<ResponseAirPortModel>?>?
}
