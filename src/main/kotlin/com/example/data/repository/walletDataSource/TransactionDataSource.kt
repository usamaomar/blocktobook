package com.example.data.repository.walletDataSource

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.publicModel.PagingApiResponse
import com.example.domain.model.transactionModel.ResponseTransactionModel

interface TransactionDataSource {
    suspend fun topUpWallet(
        userId: String,
        amount: Double,
        blockToBookFees: Double,
        chargerId: String,
        topUpType: Int,
        transactionType: Int,
    ): ApiResponse<String?>?

    suspend fun topDownWallet(
        userId: String,
        amount: Double,
        blockToBookFees: Double,
        chargerId: String,
        topUpType: Int,
        transactionType: Int,
    ): ApiResponse<String?>?

    suspend fun getWalletAmountByUserId(
        userId: String
    ): ApiResponse<String?>?


    suspend fun getAllWalletsByUserId(
        userId: String,
        pageSize: Int,
        pageNumber: Int
    ): PagingApiResponse<List<ResponseTransactionModel>?>?
}