package com.example.data.repository.paymentDataSource

import com.example.domain.model.publicModel.ApiResponse

interface PaymentDataSource {
    suspend fun createCheckout(userId : String,amountToCharge: Double,blockToBookFees: Double): ApiResponse<Boolean>?
}