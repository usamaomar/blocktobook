package com.example.data.repository.hotelDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.hotelModel.CreateHotelModel
import com.example.domain.model.hotelModel.ResponseHotelModel
import com.example.domain.model.hotelModel.UpdateHotelModel
import com.example.domain.model.hotelModel.HotelModel

interface HotelDataSource {
    suspend fun getById(id: String, xAppLanguageId: Int): ApiResponse<ResponseHotelModel?>?
    suspend fun post(hotelModel: CreateHotelModel): ApiResponse<HotelModel?>?
    suspend fun put(updateHotelModel: UpdateHotelModel): ApiResponse<HotelModel?>?
    suspend fun getAll(searchText: String,
                       pageSize: Int,
                       filterByCityId: String?,
                       pageNumber: Int,
                       xAppLanguageId: Int): PagingApiResponse<List<ResponseHotelModel>?>?
}
