package com.example.data.repository.purchaseDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.purchaseModel.CreateCustomerListModel
import com.example.domain.model.purchaseModel.CreateCustomerModel
import com.example.domain.model.purchaseModel.ResponsePurchasedHotelTicketModel

interface PurchaseDataSource {

    suspend fun checkOut(
        userId: String
    ): ApiResponse<String?>
    suspend fun createIndex()

    suspend fun checkOutSubscription(
        userId: String,
        subscriptionId: String
    ): ApiResponse<String?>

    suspend fun createOrUpdateCustomerInfo(
        userId: String, createCustomerModel: CreateCustomerModel
    ): ApiResponse<String?>
 suspend fun createOrUpdateCustomerInfoList(
        userId: String, createCustomerModel: CreateCustomerListModel
    ): ApiResponse<String?>

    suspend fun approveHotelReservation(
        userId: String, purchasedId: String, ticketNumber: String
    ): ApiResponse<String?>
    suspend fun rejectHotelReservation(
        userId: String, purchasedId: String,note: String,
    ): ApiResponse<String?>
  suspend fun cancelHotelReservation(
        userId: String, purchasedId: String,note: String,
    ): ApiResponse<String?>


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
        status: Int? = null,
    ): PagingApiResponse<List<ResponsePurchasedHotelTicketModel>?>?
    suspend fun getAllForAirlines(
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
        status: Int? = null,
    ): PagingApiResponse<List<ResponsePurchasedHotelTicketModel>?>?
    suspend fun getAllMerchantHotelReservations(
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
        status: Int? = null,
    ): PagingApiResponse<List<ResponsePurchasedHotelTicketModel>?>?

    suspend fun getAllMerchantAirlineReservations(
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
        status: Int? = null,
    ): PagingApiResponse<List<ResponsePurchasedHotelTicketModel>?>?

}