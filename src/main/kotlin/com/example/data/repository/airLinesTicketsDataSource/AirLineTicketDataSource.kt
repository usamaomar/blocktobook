package com.example.data.repository.airLinesTicketsDataSource

import com.example.domain.model.airlinesTicketModel.CreateAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.airlinesTicketModel.UpdateAirlineTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse

interface AirLineTicketDataSource {
    suspend fun post(userId: String, airLineModel: CreateAirlineTicketModel): ApiResponse<String?>?

    suspend fun put(
        userId: String,
        updateAirlineTicketModel: UpdateAirlineTicketModel
    ): ApiResponse<String?>?


    suspend fun getById(id: String): ApiResponse<ResponseAirlineTicketModel?>?

    suspend fun getAll(
        userId: String,
        searchText: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int,
        filterByDateFrom: Long? = null,
        filterByDateTo: Long? = null,
        filterByVisibility: Boolean? = null,
        filterByPriceRangeFrom: Double? = null,
        filterByPriceRangeTo: Double? = null,
        filterByAirLineIds: List<String>? = null,
    ): PagingApiResponse<List<ResponseAirlineTicketModel>?>?
}