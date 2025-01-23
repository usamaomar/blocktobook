package com.example.domain.model.purchaseModel

import io.ktor.server.auth.Principal
import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerModel(
    val purchasedHotelModelId: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val middleName: String? = null,
    val passportUrl: String? = null,
    val visaUrl: String? = null,
    val note: String? = null,
    val isInfant: Boolean? = false,
): Principal



fun CreateCustomerModel.toCustomerModel(): CustomerModel {
    return CustomerModel(
        firstName = this.firstName,
        lastName = this.lastName,
        middleName = this.middleName,
        passportUrl = this.passportUrl,
        visaUrl = this.visaUrl,
        note = this.note,
        isInfant = this.isInfant
    )
}
