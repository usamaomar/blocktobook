package com.example.data.repository.hotelTicketsDataSource

import com.example.domain.model.hotelTicketModel.CreateHotelTicketModel
import com.example.domain.model.hotelTicketModel.HotelTicketModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import com.example.domain.model.hotelTicketModel.UpdateHotelTicketModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse

interface HotelTicketDataSource {
    suspend fun post(
        userId: String,
        hotelTicketModel: CreateHotelTicketModel
    ): ApiResponse<String?>?

    suspend fun put(
        userId: String,
        updateHotelTicketModel: UpdateHotelTicketModel
    ): ApiResponse<HotelTicketModel?>?

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
        filterByHotelIds: List<String>? = null,
    ): PagingApiResponse<List<ResponseHotelTicketModel>?>?

}