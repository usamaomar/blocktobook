package com.example.domain.model.adminWalletAmount

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateAdminWalletAmount(
    val merchantId: String,
    val amount: Double,
    val checkoutId: String,
    val createdDate : Long,
) : Principal


fun CreateAdminWalletAmount.toAdminWalletAmount(): AdminWalletAmount {
    return AdminWalletAmount(
        merchantId = this.merchantId,
        amount = this.amount,
        checkoutId = this.checkoutId,
        createdDate = this.createdDate,
    )
}