package com.example.data.repository.airLinesDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.airlinesModel.AirLineModel
import com.example.domain.model.airlinesModel.CreateAirLine
import com.example.domain.model.airlinesModel.ResponseAirLineModel
import com.example.domain.model.airlinesModel.UpdateAirLine

interface AirLineDataSource {
    suspend fun getById(id: String, xAppLanguageId: Int): ApiResponse<ResponseAirLineModel?>?
    suspend fun post(airLineModel: CreateAirLine): ApiResponse<AirLineModel?>?
    suspend fun put(updateAirLineModel: UpdateAirLine): ApiResponse<AirLineModel?>?
    suspend fun getAll(searchText: String,
                       pageSize: Int,
                       pageNumber: Int,
                       xAppLanguageId: Int): PagingApiResponse<List<ResponseAirLineModel>?>?
}
