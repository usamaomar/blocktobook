package com.example.data.repository.sendGrid

import com.example.domain.model.publicModel.ApiResponse

interface SendGridDataSource {

    suspend fun sendEmailUsingSendGrid(
        userId: String,
        amount: String,
    ): ApiResponse<String?>


    suspend fun sendEmailToAllAdminsUsingSendGrid(
        merchantId: String,
        actionMessege: String,
    ): ApiResponse<String?>

    suspend fun confiramtionOfAccountApprove(
        merchantId: String,
        adminId: String,
        actionMessage: String,
    ): ApiResponse<String?>

    suspend fun sendEmailToAdminAndMerchantAfterUpdatingCustomer(
        merchantId: String,
        ticketId: String,
    ): ApiResponse<String?>
}