package com.example.data.repository.sendGrid

import com.example.domain.model.publicModel.ApiResponse
import com.example.domain.model.purchaseModel.PurchaseModel

interface SendGridDataSource {

    suspend fun sendEmailUsingSendGrid(
        userId: String,
        amount: String,
    ): ApiResponse<String?>


    suspend fun sendEmailToAllAdminsUsingSendGrid(
        merchantId: String,
        actionMessege: String,
    ): ApiResponse<String?>


    suspend fun notifyMerchantsAboutTravelUpdate(
        ownerId: String,
        ticketId: String,
        newDepartureTime: String,
        newArrivalTime: String
    ): ApiResponse<String?>

    suspend fun confiramtionOfAccountApprove(
        merchantId: String,
        adminId: String,
        actionMessage: String,
    ): ApiResponse<String?>

    suspend fun sendTestSendGrid(
        fromEmail: String,
        toEmail: String,
        text: String,
    ): ApiResponse<String?>

    suspend fun sendEmailToAdminAndMerchantAfterUpdatingCustomer(
        merchantId: String,
        ticketId: String,
    ): ApiResponse<String?>

    suspend fun notifyAdminsAndMerchantsAboutTicketPurchase(
        userId: String,
        checkoutId: String,
    ): ApiResponse<String?>
}