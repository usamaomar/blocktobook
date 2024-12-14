package com.example.data.repository.cartDataSource

import com.example.domain.model.cartModel.CreateCartModel
import com.example.domain.model.cartModel.ResponseCartModel
import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse

interface CartDataSource {

    suspend fun post(
        userId: String,
        cartModel: CreateCartModel,
        xAppLanguageId: Int
    ): ApiResponse<String?>?


    suspend fun getAll(
        userId: String,
        pageSize: Int,
        pageNumber: Int,
        xAppLanguageId: Int
    ): PagingApiResponse<List<ResponseCartModel>?>?

    suspend fun delete(
        userId: String,
        ticketId: String
    ): ApiResponse<String?>?

    suspend fun getAmount(
        userId: String,
    ): String


    suspend fun getAmountWithBlockFees(
        userId: String,
    ): String
    suspend fun getAmountWithCurrentWalletAmountWithBlockFees(
        userId: String,
        double: Double,
    ): String


    suspend fun getSubscriptionAmount(
        userId: String,
        subscriptionId: String,
    ): String

    suspend fun getAmountWithSubscriptionFees(
        userId: String,
        subscriptionId: String,
    ): String
    suspend fun getAmountWithCurrentWalletAmountWithSubscription(
        userId: String,
        double: Double,
        subscriptionId: String,
    ): String



}