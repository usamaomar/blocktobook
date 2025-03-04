package com.example.data.repository.searchDataSource

import com.example.domain.model.airlinesTicketModel.ResponseAirlineTicketModel
import com.example.domain.model.airportsModel.ResponseAirPortModel
import com.example.domain.model.cityModel.ResponseCityModel
import com.example.domain.model.hotelModel.ResponseHotelModel
import com.example.domain.model.hotelTicketModel.ResponseHotelTicketModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse

interface SearchDataSource {


    suspend fun getAllTicketsFiltration(
        userId: String,
        pageSize: Int,
        pageNumber: Int,
        filterByAdultsTicketNumber: Int? = null,
        filterByChildrenTicketNumber: Int? = null,
        filterByRoomsTicketNumber: Int? = null,
        xAppLanguageId: Int,
        filterByDateFrom: Long? = null,
        filterByDateTo: Long? = null,
        filterByHotelId: String? = null,
        filterByCityId: String? = null,
    ): PagingApiResponse<List<ResponseHotelTicketModel>?>?


    suspend fun getAllFlightTicketsFiltration(
        userId: String,
        pageSize: Int,
        pageNumber: Int,
        filterByAdultsTicketNumber: Int? = null,
        filterByChildrenTicketNumber: Int? = null,
        xAppLanguageId: Int,
        filterByDateFrom: Long? = null,
        filterByDateTo: Long? = null,
        filterByIdFromAirport: String? = null,
        filterByIdFromCity: String? = null,
        filterByIdToAirport: String? = null,
        filterByIdToCity: String? = null,
        directFlightOnly: Boolean? = null,
    ): PagingApiResponse<List<ResponseAirlineTicketModel>?>?


    suspend fun getAllMonthTicketsFiltration(
        userId: String,
        filterByAdultsTicketNumber: Int? = null,
        filterByChildrenTicketNumber: Int? = null,
        filterByRoomsTicketNumber: Int? = null,
        filterByDate: Long? = null,
        filterByHotelId: String? = null,
        filterByCityId: String? = null,
    ): PagingApiResponse<List<Long>?>


    suspend fun getAllMonthFlightTicketsFiltration(
        userId: String,
        pageSize: Int,
        pageNumber: Int,
        filterByAdultsTicketNumber: Int? = null,
        filterByChildrenTicketNumber: Int? = null,
        xAppLanguageId: Int,
        filterByDate: Long? = null,
        filterByIdFromAirport: String? = null,
        filterByIdFromCity: String? = null,
        filterByIdToAirport: String? = null,
        filterByIdToCity: String? = null,
        directFlightOnly: Boolean? = null,
    ): PagingApiResponse<List<ResponseAirlineTicketModel>?>?



    suspend fun getReturnTicketDate(
        returnTicketId: String
    ): ApiResponse<String?>?



    suspend fun getAllByCityNameAndHotelName(searchText: String,
                       pageSize: Int,
                       pageNumber: Int,
                       xAppLanguageId: Int): PagingApiResponse<List<ResponseHotelModel>?>?
    suspend fun getAllByCityNameAndAirportsName(searchText: String,
                       pageSize: Int,
                       pageNumber: Int,
                       xAppLanguageId: Int): PagingApiResponse<List<ResponseCityModel>?>?

}