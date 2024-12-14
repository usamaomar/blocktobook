package com.example.data.repository.adminWalletDataSource

import com.example.domain.model.adminWalletAmount.CreateAdminWalletAmount
import com.example.domain.model.publicModel.ApiResponse

interface AdminWalletDataSource {
    suspend fun addPurchaseToAdminWallet(
        createAdminWalletAmount: CreateAdminWalletAmount
    ): ApiResponse<String?>?
}